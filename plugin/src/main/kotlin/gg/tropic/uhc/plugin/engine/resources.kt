package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.game.listener.CgsGameEventListener
import gg.scala.cgs.game.listener.CgsGameGeneralListener
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import gg.tropic.uhc.plugin.services.scatter.ScatterService
import gg.tropic.uhc.plugin.services.scatter.remainingPlayers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
object UHCScoreboardRenderer : CgsGameScoreboardRenderer
{
    private val footerPadding = "${CC.GRAY} ".repeat(10)

    override fun getTitle() = "${CC.B_PRI}UHC"

    override fun render(
        lines: LinkedList<String>, player: Player, state: CgsGameState
    )
    {
        lines += ""

        when (state)
        {
            CgsGameState.WAITING ->
            {
                lines += "${CC.GRAY}Game being prepared"
                lines += "${CC.GRAY}while waiting for more"
                lines += "${CC.GRAY}players$ellipsis"
                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
                lines += "${CC.WHITE}Mode: ${CC.GOLD}FFA"
                lines += "${CC.WHITE}Players: ${CC.GOLD}${
                    Bukkit.getOnlinePlayers().size
                }/${
                    Bukkit.getMaxPlayers()
                }"
            }

            CgsGameState.STARTING ->
            {
                lines += "${CC.GRAY}Scattering players$ellipsis"
                lines += ""
                lines += "${CC.GRAY}Game will be starting"
                lines += "${CC.GRAY}in ${CC.WHITE}${
                    TimeUtil.formatIntoMMSS(StartingStateRunnable.PRE_START_TIME)
                }${CC.GRAY}"
                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
                lines += "${CC.WHITE}Mode: ${CC.GOLD}FFA"
                lines += "${CC.WHITE}Scattered: ${CC.GOLD}${ScatterService.playersScattered.size}/${ScatterService.gameFillCount}"
            }

            CgsGameState.STARTED ->
            {
                lines += "Game time: ${CC.GOLD}${
                    TimeUtil.formatIntoMMSS((CgsGameEngine.INSTANCE.gameStart / 1000).toInt())
                }"
                lines += "Remaining: ${CC.GOLD}${
                    remainingPlayers.size
                }/${
                    ScatterService.gameFillCount
                }"
                lines += "Kills: ${CC.GOLD}${
                    CgsGameEngine.INSTANCE
                        .getStatistics(
                            CgsPlayerHandler.find(player)!!
                        )
                        .gameKills.value
                }"
                lines += "Border: ${CC.GOLD}500 ${CC.GRAY}(50m)"
            }

            CgsGameState.ENDED ->
            {
                lines += "${CC.GREEN}Congrats to ${CC.BOLD}Your Mother"
                lines += "${CC.GREEN}for winning this UHC"
                lines += "${CC.GREEN}game!"
                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
            }

            else ->
            {
            }
        }

        lines += ""
        lines += "${CC.GRAY}tropic.gg $footerPadding"
    }
}
