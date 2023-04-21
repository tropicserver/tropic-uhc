package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.states.CgsGameState
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
    override fun getTitle() = "${CC.B_PRI}UHC"

    override fun render(
        lines: LinkedList<String>, player: Player, state: CgsGameState
    )
    {
        lines += ""


        lines += "${CC.WHITE}Host: ${CC.RED}None"
        lines += "${CC.WHITE}Mode: ${CC.GOLD}FFA"
        lines += "${CC.WHITE}Players: ${CC.GOLD}${
            Bukkit.getOnlinePlayers().size
        }/${
            Bukkit.getMaxPlayers()
        }"
        lines += ""
        lines += "${CC.GRAY}tropic.gg ${CC.GRAY}   ${CC.GRAY}   ${CC.GRAY}   ${CC.GRAY}"
    }
}
