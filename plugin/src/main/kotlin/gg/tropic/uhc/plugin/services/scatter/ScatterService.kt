package gg.tropic.uhc.plugin.services.scatter

import com.cryptomorin.xseries.XMaterial
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.handler.CgsSpectatorHandler
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.game.command.SpectateCommand
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.LemonConstants
import gg.tropic.uhc.plugin.TropicUHCPlugin
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
import gg.tropic.uhc.plugin.services.map.threadlock.ThreadLockUtilities
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import gg.tropic.uhc.plugin.services.scenario.menu.ScenarioMenu
import gg.tropic.uhc.plugin.services.scenario.profile
import gg.tropic.uhc.plugin.services.styles.prefix
import gg.tropic.uhc.shared.UHCGameInfo
import me.lucko.helper.Events
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
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

    val playersScattered: List<Player>
        get() = Players.all()
            .filter {
                it.hasMetadata("scattered")
            }

    val playersNotYetScattered: List<Player>
        get() = Players.all()
            .filter {
                !it.hasMetadata("scattered") && !it.hasMetadata("spectator")
            }

    var gameFillCount = 0
    var gracePeriodActive = true

    @Configure
    fun configure()
    {
        // whitelist by default to prevent non-hosts from logging in
        Bukkit.setWhitelist(true)

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

        Events
            .subscribe(CgsGameEngine.CgsGameEndEvent::class.java)
            .handler {
                File(Bukkit.getWorldContainer(), "tropic.uhc.lock").delete()
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGamePreStartCancelEvent::class.java)
            .handler {
                Bukkit.getOnlinePlayers()
                    .forEach {
                        it.sit(false)

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

        fun Player.applyLobbyItems()
        {
            delayed(4L) {
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

                inventory.setItem(
                    8, ItemBuilder
                        .of(Material.REDSTONE_COMPARATOR)
                        .name("${CC.AQUA}Settings ${CC.GRAY}(Right Click)")
                        .build()
                )
                inventory.setItem(
                    4, ItemBuilder
                        .of(XMaterial.FIRE_CHARGE)
                        .name("${CC.GREEN}Spectate ${CC.GRAY}(Right Click)")
                        .build()
                )
                updateInventory()
            }
        }

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
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED && it.entity is Player
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
                it.participant.sendMessage("$prefix${CC.WHITE}Today's game host: ${hostDisplayName()}")

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
            .subscribe(CgsGameEngine.CgsGameParticipantConnectEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING
            }
            .handler {
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
                        ConfigurateMenu().openMenu(it.player)
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

                playersScattered
                    .forEach {
                        unsitPlayer(it)

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
                Bukkit.broadcastMessage("$prefix${CC.GOLD}Our rules are posted at ${CC.BOLD}tropic.gg/uhc/rules${CC.GOLD}, please acknowledge them.")

                WorldBorderService.currentSize = initialBorderSize
                    .value.toDouble()
                BorderUpdateEventExecutor.start()

                GameScenarioService.scenarios
                    .filterValues { it.enabled }
                    .forEach {
                        it.value.configure()
                        plugin.server.pluginManager
                            .registerEvents(it.value, plugin)
                    }

                createRunner(
                    (finalHeal.value * 60) + 1,
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

                createRunner(
                    (gracePeriod.value * 60) + 1,
                    {
                        UHCGameInfo.disqualifyOnLogout = true
                        gracePeriodActive = false
                        Bukkit.broadcastMessage("${CC.GREEN}Grace Period has ended! ${CC.BOLD}You can now PvP others. Good luck!")

                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "unmutechat"
                        )
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

                Bukkit.setWhitelist(true)
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "mutechat"
                )
                Bukkit.broadcastMessage("$prefix${CC.GREEN}The server is no longer allowing new players to join. Players will now be scattered.")

                // we calculate pre start time in ticks so we're
                // going to convert it to 20, and add another second
                StartingStateRunnable.PRE_START_TIME = (estimatePreStartTime() / 20) + 1

                thread {
                    while (
                        CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING &&
                        playersNotYetScattered.isNotEmpty()
                    )
                    {
                        val firstNotScattered = playersNotYetScattered.first()
                        ThreadLockUtilities.runMainLock {
                            firstNotScattered.scatter()
                        }

                        sleep(50L)
                    }
                }
            }
            .bindWith(plugin)
    }

    /**
     * Estimate a pre-start time based on the count of players who need to be
     * scattered. We also add 10 seconds to compensate for any lag/other issues
     * that may occur and delay the scattering process.
     */
    fun estimatePreStartTime() = (playersNotYetScattered.size * 5) + (20 * 20)

    fun Player.scatter()
    {
        resetAttributes()
        teleport(MapGenerationService.generateScatterLocation())
        sitPlayer(player = this)

        if (starterFood.value != 0)
        {
            inventory.addItem(
                ItemStack(Material.COOKED_BEEF, starterFood.value)
            )
        }

        setMetadata(
            "scattered",
            FixedMetadataValue(CgsGameEngine.INSTANCE.plugin, true)
        )
    }
}
