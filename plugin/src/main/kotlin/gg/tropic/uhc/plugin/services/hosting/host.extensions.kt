package gg.tropic.uhc.plugin.services.hosting

import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
fun Player.isHost() = HostService.gameHost == uniqueId

fun hostDisplayName() = if (HostService.gameHost == null)
    "${CC.RED}None" else QuickAccess.coloredName(HostService.gameHost!!)
