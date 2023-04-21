package gg.tropic.uhc.plugin.services.hosting

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
fun Player.isHost() = HostService.gameHost == uniqueId
