package gg.tropic.uhc.plugin.services.hosting

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.hosting.menu.HostSetupMenu
import gg.tropic.uhc.plugin.services.scatter.ScatterService
import gg.tropic.uhc.plugin.services.styles.prefix
import gg.tropic.uhc.plugin.services.teams.gameType
import gg.tropic.uhc.plugin.services.teams.team
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
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
        Events
            .subscribe(PlayerJoinEvent::class.java)
            .expireAfter(1)
            .handler {
                if (it.player.hasPermission("uhc.command.host"))
                {
                    delayed(1L) {
                        it.player.chat("/host")
                        HostSetupMenu().openMenu(it.player)
                    }
                }
            }
            .bindWith(plugin)

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

        delayed(10L) {
            val prevProvider = DefaultChatChannel.chatTagProvider
            DefaultChatChannel.chatTagProvider = ctx@{ player ->
                var component = Component.text()

                if (gameHost == player.uniqueId)
                    component = component
                        .append(
                            Component.text(" [", NamedTextColor.GRAY)
                        )
                        .append(
                            Component.text("Host", NamedTextColor.GOLD)
                        )
                        .append(
                            Component.text("]", NamedTextColor.GRAY)
                        )

                if (gameType.teamSize > 1 && player.team != null)
                    component = component
                        .append(
                            Component.text(" [", NamedTextColor.GRAY)
                        )
                        .append(
                            Component.text("#${player.team!!.id}", NamedTextColor.GREEN)
                        )
                        .append(
                            Component.text("]", NamedTextColor.GRAY)
                        )

                component.append(prevProvider.invoke(player)).build()
            }
        }
    }
}
