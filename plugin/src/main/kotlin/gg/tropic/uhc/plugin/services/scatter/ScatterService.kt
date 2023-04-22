package gg.tropic.uhc.plugin.services.scatter

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Events
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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
        // TODO: scatter logic & player reset
        //   ride on bat
    }
}
