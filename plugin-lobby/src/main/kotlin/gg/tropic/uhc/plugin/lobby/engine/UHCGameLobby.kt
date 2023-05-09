package gg.tropic.uhc.plugin.lobby.engine

import gg.glade.core.game.coins.CoinProfileManager
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.lobby.command.commands.RecentGamesCommand
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.tangerine.tracking.TangerinePlayerTracker
import gg.tropic.uhc.shared.UHCGameInfo
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
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
            .name("${CC.GREEN}UHC Game ${info.internalServerId}")
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
        override fun getLines(lines: LinkedList<String>, player: Player)
        {
            lines += "${CC.GRAY}${CC.STRIKE_THROUGH}----------------"
            lines += "${CC.PRI}Players:"
            lines += "${CC.WHITE}${
                Numbers.format(TangerinePlayerTracker.globalPlayers)
            }"
            lines += ""
            lines += "${CC.PRI}Coins:"
            lines += "${CC.GOLD}${
                Numbers.format(
                    CoinProfileManager.find(player)?.coins ?: 0
                )
            }"
            lines += "${CC.GRAY}${CC.STRIKE_THROUGH}----------------"
            lines += "${CC.PRI}Glade.GG"
        }

        override fun getTitle(player: Player) = "${CC.B_PRI}Glade ${CC.B_WHITE}Network"
    }

    override fun getScoreboardAdapter() = scoreboardAdapter
}
