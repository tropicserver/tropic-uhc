package gg.tropic.uhc.plugin.services.scatter

import com.cryptomorin.xseries.XMaterial
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.combat.CombatLogService
import gg.scala.cgs.common.player.handler.CgsSpectatorHandler
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.teams.CgsGameTeam
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.LemonConstants
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.autonomous
import gg.tropic.uhc.plugin.engine.createRunner
import gg.tropic.uhc.plugin.services.border.BorderUpdateEventExecutor
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.border.WorldBorderService.ensureWithinBorderBounds
import gg.tropic.uhc.plugin.services.configurate.finalHeal
import gg.tropic.uhc.plugin.services.configurate.gracePeriod
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.configurate.menu.ConfigurateMenu
import gg.tropic.uhc.plugin.services.configurate.starterFood
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.map.mapNetherWorld
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.map.threadlock.ThreadLockUtilities
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import gg.tropic.uhc.plugin.services.scenario.menu.ScenarioMenu
import gg.tropic.uhc.plugin.services.scenario.profile
import gg.tropic.uhc.plugin.services.styles.prefix
import gg.tropic.uhc.plugin.services.teams.gameType
import gg.tropic.uhc.shared.UHCGameInfo
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import net.evilblock.cubed.visibility.VisibilityHandler
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object ScatterService
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    val scatteredTeams = mutableSetOf<Int>()

    val populatedTeams: List<CgsGameTeam>
        get() = CgsGameTeamService
            .teams.values
            .filter {
                it.participants.isNotEmpty()
            }

    val teamsScattered: List<CgsGameTeam>
        get() = scatteredTeams
            .map {
                CgsGameTeamService.teams[it]!!
            }

    val teamsNotYetScattered: List<CgsGameTeam>
        get() = populatedTeams
            .filter {
                it.id !in scatteredTeams && it.alive.isNotEmpty()
            }

    var gameFillCount = 0
    var gracePeriodActive = true

    var lobbyItemApplication = { player: Player -> }
    var postScatterLogic = { player: Player -> }

    var pvpTime: Long? = null

    @Configure
    fun configure()
    {
        // whitelist by default to prevent non-hosts from logging in
        if (!autonomous)
        {
            Bukkit.setWhitelist(true)
        }

        Events
            .subscribe(PlayerDropItemEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING ||
                        CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING
            }
            .handler {
                it.isCancelled = true
            }
            .bindWith(plugin)

        fun Player.applyLobbyItems()
        {
            delayed(4L) {
                if (!autonomous)
                {
                    inventory.setItem(
                        0, ItemBuilder
                            .of(Material.BOOK)
                            .name("${CC.GOLD}Game Config ${CC.GRAY}(Right Click)")
                            .build()
                    )

                    inventory.setItem(
                        1, ItemBuilder
                            .of(Material.JUKEBOX)
                            .name("${CC.D_AQUA}Scenarios ${CC.GRAY}(Right Click)")
                            .build()
                    )
                }

                inventory.setItem(
                    8, ItemBuilder
                        .of(Material.REDSTONE_COMPARATOR)
                        .name("${CC.AQUA}Settings ${CC.GRAY}(Right Click)")
                        .build()
                )

                inventory.setItem(
                    7, ItemBuilder
                        .of(Material.SKULL_ITEM)
                        .name("${CC.GREEN}Profile ${CC.GRAY}(Right Click)")
                        .build()
                )
                inventory.setItem(
                    4, ItemBuilder
                        .of(XMaterial.FIRE_CHARGE)
                        .name("${CC.GREEN}Spectate ${CC.GRAY}(Right Click)")
                        .build()
                )

                lobbyItemApplication(this)
                updateInventory()
            }
        }

        Events
            .subscribe(CgsGameEngine.CgsGameEndEvent::class.java)
            .handler {
                File(Bukkit.getWorldContainer(), "tropic.uhc.lock").delete()
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGamePreStartCancelEvent::class.java)
            .handler {
                scatteredTeams.clear()

                Bukkit.getOnlinePlayers()
                    .forEach {
                        it.resetAttributes()
                        it.sit(false)
                        it.applyLobbyItems()
                        it.removeMetadata("scattered", plugin)

                        if (it.world.name == "uhc_world")
                        {
                            it.teleport(
                                CgsGameEngine.INSTANCE.gameArena!!
                                    .getPreLobbyLocation()
                            )
                        }
                    }

                Bukkit.broadcastMessage(
                    "$prefix${CC.RED}The game is no longer starting due to a lack of players!"
                )
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameSpectatorRemoveEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING
            }
            .handler {
                it.spectator.applyLobbyItems()
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameSpectatorAddEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING
            }
            .handler {
                it.spectator.sit(false)
                it.spectator.teleport(
                    CgsGameEngine.INSTANCE.gameArena!!.getSpectatorLocation()
                )
            }
            .bindWith(plugin)

        Events
            .subscribe(EntityDamageByEntityEvent::class.java)
            .expireIf {
                !gracePeriodActive
            }
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED &&
                        (it.entity is Player || CombatLogService.combatLog(it.entity) != null)
            }
            .handler {
                it.isCancelled = true
                it.damager.sendMessage("${CC.RED}You cannot hurt other players during the grace period.")
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameParticipantConnectEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING
            }
            .handler {
                it.participant.sendMessage("$prefix${CC.GREEN}Welcome to ${LemonConstants.SERVER_NAME}'s UHC!")
                it.participant.sendMessage("$prefix${CC.GRAY}Please report any bugs/issues in our Discord server!")

                if (gameType.teamSize < 2)
                {
                    it.participant.sendMessage("$prefix${CC.WHITE}Today's game host: ${hostDisplayName()}")
                } else
                {
                    it.participant.sendMessage(
                        "$prefix${CC.PINK}You've joined a team-based ${gameType.name} game. Use ${CC.WHITE}/team${CC.PINK} to form your team before the game starts."
                    )
                }

                it.participant.applyLobbyItems()
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameParticipantReinstateEvent::class.java)
            .handler {
                it.participant.ensureWithinBorderBounds(
                    WorldBorderService.currentSize.toInt()
                )
            }
            .bindWith(plugin)

        Events
            .subscribe(
                CgsGameEngine.CgsGameParticipantConnectEvent::class.java,
                EventPriority.HIGHEST
            )
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING
            }
            .handler {
                it.isCancelled = true

                CgsSpectatorHandler.setSpectator(
                    it.participant, false
                )

                delayed(1L) {
                    it.participant.sendMessage("${CC.D_RED}✘ ${CC.RED}This is due to your late connection to the server. Players are already being scattered.")
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() && it.action.name.contains("RIGHT") && it.clickedBlock == null &&
                        CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING &&
                        !it.player.hasMetadata("spectating")
            }
            .handler {
                when (it.item!!.type)
                {
                    Material.BOOK ->
                    {
                        if (!autonomous)
                        {
                            ConfigurateMenu().openMenu(it.player)
                        }
                    }

                    Material.JUKEBOX ->
                    {
                        ScenarioMenu().openMenu(it.player)
                    }

                    Material.FIREBALL ->
                    {
                        it.player.chat("/spectate confirm")
                    }

                    Material.REDSTONE_COMPARATOR ->
                    {
                        it.player.chat("/settings")
                    }

                    Material.SKULL_ITEM ->
                    {
                        it.player.chat("/profile")
                    }

                    else ->
                    {
                    }
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameStartEvent::class.java)
            .handler {
                Bukkit.setWhitelist(false)

                teamsScattered
                    .flatMap {
                        it.alivePlayers
                    }
                    .forEach {
                        unsitPlayer(it)

                        if (autonomous)
                        {
                            it.addPotionEffect(
                                PotionEffect(PotionEffectType.FIRE_RESISTANCE, gracePeriod.value * 60, 0)
                            )
                        }

                        it.profile.apply {
                            limDiamond = 0
                            limGold = 0
                            limIron = 0

                            ironMined.reset()
                            lapisMined.reset()
                            redstoneMined.reset()
                            spawnersMined.reset()
                            coalMined.reset()
                            diamondsMined.reset()
                            goldMined.reset()
                        }
                    }

                Bukkit.broadcastMessage("$prefix${CC.GRAY}This gamemode is currently in BETA!")
                Bukkit.broadcastMessage("$prefix${CC.GRAY}Please report any bugs/issues in our Discord server!")
                Bukkit.broadcastMessage("$prefix${CC.GOLD}Our rules are posted at ${CC.BOLD}${
                    LemonConstants.WEB_LINK
                }/uhc/rules${CC.GOLD}, please acknowledge them.")

                WorldBorderService.currentSize = initialBorderSize
                    .value.toDouble()

                if (!autonomous)
                {
                    BorderUpdateEventExecutor.start()
                } else
                {
                    WorldBorderService
                        .pushSizeUpdate(
                            WorldBorderService.currentSize
                        )
                }

                GameScenarioService.scenarios
                    .filterValues { it.enabled }
                    .forEach {
                        it.value.configure()
                        plugin.server.pluginManager
                            .registerEvents(it.value, plugin)
                    }

                val multiplier = (if (!autonomous) 60 else 1)

                if (!autonomous)
                {
                    createRunner(
                        (finalHeal.value * multiplier) + 1,
                        {
                            remainingPlayers.forEach {
                                it.health = it.maxHealth
                            }

                            Bukkit.broadcastMessage("${CC.GREEN}Final Heal has occurred! ${CC.BOLD}Good luck!")
                        },
                        {
                            Bukkit.broadcastMessage(
                                "${CC.SEC}Final Heal will occur in ${CC.PRI}${
                                    DurationFormatUtils.formatDurationWords((it * 1000).toLong(), true, true)
                                }${CC.SEC}."
                            )
                        }
                    )
                }

                pvpTime = System.currentTimeMillis() + ((gracePeriod.value * multiplier) + 1) * 1000

                createRunner(
                    (gracePeriod.value * multiplier) + 1,
                    {
                        UHCGameInfo.disqualifyOnLogout = true
                        gracePeriodActive = false
                        Bukkit.broadcastMessage("${CC.GREEN}Grace Period has ended! ${CC.BOLD}You can now PvP others. Good luck!")

                        listOf(mapWorld(), mapNetherWorld())
                            .forEach {
                                it.worldBorder.setSize(100.0, 2700)
                            }

                        if (autonomous)
                        {
                            Bukkit.getOnlinePlayers()
                                .forEach { player ->
                                    player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE)
                                }
                        }

                        if (!autonomous)
                        {
                            Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "unmutechat"
                            )
                        }
                    },
                    {
                        Bukkit.broadcastMessage(
                            "${CC.SEC}Grace Period ends in ${CC.PRI}${
                                DurationFormatUtils.formatDurationWords((it * 1000).toLong(), true, true)
                            }${CC.SEC}."
                        )
                    }
                )
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGamePreStartEvent::class.java)
            .handler {
                gameFillCount = Bukkit.getOnlinePlayers()
                    .count {
                        !it.hasMetadata("spectator")
                    }

                Bukkit.getOnlinePlayers()
                    .filter {
                        it.hasMetadata("spectator")
                    }
                    .forEach { spectator ->
                        spectator.teleport(
                            CgsGameEngine.INSTANCE.gameArena!!.getSpectatorLocation()
                        )
                    }

                if (!autonomous)
                {
                    Bukkit.setWhitelist(true)
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "mutechat"
                    )
                }

                Bukkit.broadcastMessage("$prefix${CC.GREEN}The server is no longer allowing new players to join. Players will now be scattered.")

                // we calculate pre start time in ticks so we're
                // going to convert it to 20, and add another second
                StartingStateRunnable.PRE_START_TIME = (estimatePreStartTime() / 20) + 1

                Schedulers
                    .sync()
                    .runRepeating({ task ->
                        if (
                            CgsGameEngine.INSTANCE.gameState != CgsGameState.STARTING ||
                            teamsNotYetScattered.isEmpty()
                        )
                        {
                            task.closeAndReportException()
                            return@runRepeating
                        }

                        val firstNotScattered = teamsNotYetScattered.first()
                        scatteredTeams.add(firstNotScattered.id)
                        firstNotScattered.scatter()
                    }, 0L, 10)
            }
            .bindWith(plugin)
    }

    /**
     * Estimate a pre-start time based on the count of players who need to be
     * scattered. We also add 10 seconds to compensate for any lag/other issues
     * that may occur and delay the scattering process.
     */
    fun estimatePreStartTime() = (teamsNotYetScattered.map { it.participants.size }.count() * 11) + (20 * 20)

    fun Player.scatter(
        scatterLocation: Location =
            MapGenerationService.generateScatterLocation()
    )
    {
        resetAttributes()
        teleport(scatterLocation)
        sitPlayer(player = this)

        if (starterFood.value != 0)
        {
            inventory.addItem(
                ItemStack(Material.COOKED_BEEF, starterFood.value)
            )
        }

        setMetadata(
            "scattered",
            FixedMetadataValue(plugin, true)
        )

        VisibilityHandler.update(player)
    }
}
