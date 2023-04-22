package gg.tropic.uhc.plugin.services.scenario

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
val Player.playing: Boolean
    get() = hasMetadata("spectator")

val Player.profile: UHCPlayerModel
    get() = CgsGameEngine.INSTANCE
        .getStatistics(
            CgsPlayerHandler.find(player)!!
        ) as UHCPlayerModel

fun goldenHead() = ItemBuilder(Material.GOLDEN_APPLE)
    .data(0)
    .name(CC.GOLD + "Golden Head")
    .build()
