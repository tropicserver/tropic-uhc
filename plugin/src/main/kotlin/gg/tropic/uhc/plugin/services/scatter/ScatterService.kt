package gg.tropic.uhc.plugin.services.scatter

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.configurate.starterFood
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

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
                !it.hasMetadata("scattered")
            }

    var gameFillCount = 0

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerDropItemEvent::class.java)
            .filter {
                CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTING
            }
            .handler {
                it.isCancelled = true
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGameStartEvent::class.java)
            .handler {
                Bukkit.setWhitelist(false)

                playersScattered
                    .forEach {
                        unsitPlayer(it)
                    }

                Bukkit.broadcastMessage("$prefix${CC.GRAY}This gamemode is currently in BETA!")
                Bukkit.broadcastMessage("$prefix${CC.GRAY}Please report any bugs/issues in our Discord server!")
                Bukkit.broadcastMessage("$prefix${CC.GOLD}Our rules are posted at ${CC.BOLD}tropic.gg/uhc/rules${CC.GOLD}, please acknowledge them.")
            }
            .bindWith(plugin)

        Events
            .subscribe(CgsGameEngine.CgsGamePreStartEvent::class.java)
            .handler {
                // TODO: exclude staff and host?
                gameFillCount = Bukkit.getOnlinePlayers().size

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
