package gg.tropic.uhc.plugin.services.hosting

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object HostService
{
    var gameHost: UUID? = null

    @Inject
    lateinit var plugin: TropicUHCPlugin

    @Configure
    fun configure()
    {
        val prevProvider = DefaultChatChannel.chatTagProvider
        DefaultChatChannel.chatTagProvider = { player ->
            if (gameHost == player.uniqueId)
                Component
                    .text(" ")
                    .append(
                        Component.text("[", NamedTextColor.GRAY)
                    )
                    .append(
                        Component.text("Host", NamedTextColor.GOLD)
                    )
                    .append(
                        Component.text("]", NamedTextColor.GRAY)
                    )

            prevProvider.invoke(player)
        }

        Events
            .subscribe(PlayerQuitEvent::class.java)
            .filter {
                it.player.uniqueId == gameHost
            }
            .handler {
                gameHost = null
                Bukkit.broadcastMessage("$prefix${CC.RED}The host logged out of the server!")
            }
            .bindWith(plugin)
    }
}
