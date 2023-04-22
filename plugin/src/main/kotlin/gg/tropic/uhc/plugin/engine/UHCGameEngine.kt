package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.nametag.CgsGameNametagAdapter
import gg.scala.cgs.common.player.visibility.CgsGameVisibilityAdapter
import gg.scala.cgs.common.snapshot.CgsGameSnapshot
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.shared.UHCGameInfo
import gg.tropic.uhc.shared.gamemode.UHCSoloGameMode
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.visibility.VisibilityAction

var ellipsis = "."
var ellipsisIndex = 0

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
    init
    {
        Tasks.timer(0L, 20L) {
            if (ellipsisIndex > 3)
            {
                ellipsisIndex = 1
            }

            ellipsis = ".".repeat(ellipsisIndex)
            ellipsisIndex += 1
        }
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
