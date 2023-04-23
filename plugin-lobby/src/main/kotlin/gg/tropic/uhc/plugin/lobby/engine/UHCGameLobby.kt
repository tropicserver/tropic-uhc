package gg.tropic.uhc.plugin.lobby.engine

import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.lobby.command.commands.RecentGamesCommand
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.uhc.shared.UHCGameInfo
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
class UHCGameLobby : CgsGameLobby<UHCPlayerModel>(UHCPlayerModel::class)
{
    override fun getFormattedButton(info: CgsServerInstance, player: Player) =
        ItemBuilder
            .of(Material.BOOK_AND_QUILL)
            .name("${CC.GREEN}${info.internalServerId}")
            .addToLore(
                "${CC.GRAY}Alive: ${info.gameServerInfo!!.participants.size}",
                "",
                "${CC.YELLOW}Click to join!"
            )
            .toButton{ _, _ ->
                VelocityRedirectSystem.redirect(player, info.internalServerId)
            }

    override fun getGameInfo() = UHCGameInfo

    override fun getGameModeButtons() = mutableMapOf(
        13 to ItemBuilder
            .of(Material.GOLDEN_APPLE)
            .name("${CC.GREEN}UHC")
            .addToLore(
                "${CC.GRAY}Hosted UHC games!",
                "",
                "${CC.GRAY}Servers: ${CC.GREEN}1",
                "",
                "${CC.GREEN}Click to join!"
            )
            .toButton { player, _ ->
                RecentGamesCommand.onJoinGame(player!!, "solo")
            }
    )

    private val kills = object : CgsLobbyRankingEntry
    {
        override fun getDisplay() = "Kills"
        override fun getId() = "kills"
        override fun getStatLabel() = "kills"
    }

    private val wins = object : CgsLobbyRankingEntry
    {
        override fun getDisplay() = "Wins"
        override fun getId() = "wins"
        override fun getStatLabel() = "wins"
    }

    override fun getRankingEntries() = listOf(kills, wins)

    private val scoreboardAdapter = object : ScoreboardAdapter()
    {
        private val footerPadding = "${CC.GRAY} ".repeat(10)

        override fun getLines(lines: LinkedList<String>, player: Player)
        {
            lines += "${CC.WHITE}Lobby: ${CC.GOLD}${
                CgsGameInfoUpdater.lobbyTotalCount
            }"
            lines += "${CC.WHITE}In-Game: ${CC.GOLD}${
                CgsGameInfoUpdater.playingTotalCount
            }"
            lines += ""
            lines += "${CC.GRAY}${LemonConstants.WEB_LINK} $footerPadding"
        }

        override fun getTitle(player: Player) = "${CC.B_PRI}UHC"
    }

    override fun getScoreboardAdapter() = scoreboardAdapter
}
