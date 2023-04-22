package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.states.CgsGameState
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import net.evilblock.cubed.util.CC
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
            CgsGameState.WAITING -> {
                lines += "${CC.GRAY}Game being prepared"
                lines += "${CC.GRAY}while waiting for more"
                lines += "${CC.GRAY}players$ellipsis"
            }
            else -> {}
        }

        lines += ""
        lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
        lines += "${CC.WHITE}Mode: ${CC.GOLD}FFA"
        lines += "${CC.WHITE}Players: ${CC.GOLD}${
            Bukkit.getOnlinePlayers().size
        }/${
            Bukkit.getMaxPlayers()
        }"
        lines += ""
        lines += "${CC.GRAY}tropic.gg $footerPadding"
    }
}
