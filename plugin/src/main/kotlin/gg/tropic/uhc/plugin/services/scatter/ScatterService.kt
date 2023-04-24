package gg.tropic.uhc.plugin.services.scatter

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.LemonConstants
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.engine.createRunner
import gg.tropic.uhc.plugin.services.border.BorderUpdateEventExecutor
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.configurate.finalHeal
import gg.tropic.uhc.plugin.services.configurate.gracePeriod
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.configurate.menu.ConfigurateMenu
import gg.tropic.uhc.plugin.services.configurate.starterFood
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.map.mapNetherWorld
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import gg.tropic.uhc.plugin.services.scenario.menu.ScenarioMenu
import gg.tropic.uhc.plugin.services.scenario.profile
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.io.File

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

    @Configure
    fun configure()
    {
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
            .subscribe(PlayerJoinEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING
            }
            .handler {
                it.player.sendMessage("$prefix${CC.GREEN}Welcome to ${LemonConstants.SERVER_NAME}'s UHC!")
                it.player.sendMessage("$prefix${CC.GRAY}Please report any bugs/issues in our Discord server!")
                it.player.sendMessage("$prefix${CC.WHITE}Today's game host: ${hostDisplayName()}")

                Tasks.delayed(3L) {
                    it.player.inventory.setItem(
                        0, ItemBuilder
                            .of(Material.BOOK)
                            .name("${CC.GOLD}Game Config ${CC.GRAY}(Right Click)")
                            .build()
                    )

                    it.player.inventory.setItem(
                        1, ItemBuilder
                            .of(Material.EYE_OF_ENDER)
                            .name("${CC.GOLD}Scenarios ${CC.GRAY}(Right Click)")
                            .build()
                    )
                    it.player.updateInventory()
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() && it.action.name.contains("RIGHT") && it.clickedBlock == null &&
                        CgsGameEngine.INSTANCE.gameState == CgsGameState.WAITING
            }
            .handler {
                when (it.item!!.type)
                {
                    Material.BOOK ->
                    {
                        ConfigurateMenu().openMenu(it.player)
                    }

                    Material.EYE_OF_ENDER ->
                    {
                        ScenarioMenu().openMenu(it.player)
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

                        Bukkit.broadcastMessage("${CC.GREEN}Final heal has occurred! ${CC.BOLD}Good luck!")
                    },
                    {
                        Bukkit.broadcastMessage(
                            "${CC.SEC}Final heal occurs in ${CC.PRI}${
                                DurationFormatUtils.formatDurationWords((it * 1000).toLong(), true, true)
                            }${CC.SEC}."
                        )
                    }
                )

                createRunner(
                    (gracePeriod.value * 60) + 1,
                    {
                        mapWorld().pvp = true
                        mapNetherWorld().pvp = true

                        Bukkit.broadcastMessage("${CC.GREEN}Grace Period has ended! You can now PvP others. ${CC.BOLD}Good luck!")
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

                Bukkit.setWhitelist(true)
                Bukkit.broadcastMessage("$prefix${CC.GREEN}The server is no longer allowing new players to join. Players will now be scattered.")

                // we calculate pre start time in ticks so we're
                // going to convert it to 20, and add another second
                StartingStateRunnable.PRE_START_TIME =
                    (estimatePreStartTime() / 20) + 1

                Schedulers
                    .sync()
                    .runRepeating(
                        { task ->
                            if (CgsGameEngine.INSTANCE.gameState != CgsGameState.STARTING)
                            {
                                task.closeAndReportException()
                                return@runRepeating
                            }

                            if (playersNotYetScattered.isEmpty())
                            {
                                task.closeAndReportException()
                                return@runRepeating
                            }

                            val firstNotScattered = playersNotYetScattered.first()
                            firstNotScattered.scatter()
                        },
                        // we have some leeway here, so we'll go for 4 ticks
                        // instead of the 5 we have time for.
                        0L, 4L
                    )
            }
            .bindWith(plugin)
    }

    /**
     * Estimate a pre-start time based on the count of players who need to be
     * scattered. We also add 10 seconds to compensate for any lag/other issues
     * that may occur and delay the scattering process.
     */
    fun estimatePreStartTime() = (playersNotYetScattered.size * 5) + (15 * 20)

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
