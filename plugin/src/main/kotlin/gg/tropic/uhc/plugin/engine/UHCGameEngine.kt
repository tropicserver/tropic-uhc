package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.nametag.CgsGameNametagAdapter
import gg.scala.cgs.common.player.visibility.CgsGameVisibilityAdapter
import gg.scala.cgs.common.snapshot.CgsGameSnapshot
import gg.scala.cgs.common.voting.VotingMapConfiguration
import gg.scala.cgs.common.voting.VotingMapEntry
import gg.scala.cgs.common.voting.selection.VoteSelectionType
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.shared.UHCGameInfo
import gg.tropic.uhc.shared.gamemode.UHCSoloGameMode
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.visibility.VisibilityAction
import org.bukkit.Location
import java.time.Duration

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
class UHCGameEngine(
    plugin: TropicUHCPlugin
) : CgsGameEngine<UHCPlayerModel>(
    plugin,
    UHCGameInfo,
    UHCSoloGameMode,
    UHCPlayerModel::class
)
{
    override fun getVotingConfig() = object : VotingMapConfiguration
    {
        override val minimumPlayersForVotingStart = 1
        override val selectionType = VoteSelectionType.GUI
        override val votingAutoCloseDuration = Duration.ofSeconds(61)

        override fun entries() = listOf<VotingMapEntry>()
        override fun preStartLobby() = UHCGameInfo.gameModes.first()
            .getArenas()
            .first()
            .getPreLobbyLocation()
    }

    override fun getGameSnapshot() = object : CgsGameSnapshot
    {
        override fun getExtraInformation() = emptyList<String>()
    }

    override fun getNametagAdapter() = object : CgsGameNametagAdapter
    {
        override fun computeNametag(viewer: CgsGamePlayer, target: CgsGamePlayer) = null
    }

    override fun getScoreboardRenderer() = UHCScoreboardRenderer

    override fun getVisibilityAdapter() = object : CgsGameVisibilityAdapter
    {
        override fun computeVisibility(viewer: CgsGamePlayer, target: CgsGamePlayer) = VisibilityAction.NEUTRAL
    }
}
