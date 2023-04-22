package gg.tropic.uhc.plugin.services.configurate

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.TropicUHCPlugin
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object ConfigurateService
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerPortalEvent::class.java)
            .filter { !nether.value }
            .handler {
                it.isCancelled = true
                it.player.sendMessage("${CC.RED}Nether is disabled during this game!")
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerItemConsumeEvent::class.java)
            .filter { !absorption.value }
            .handler {
                delayed(3L) {
                    it.player.removePotionEffect(PotionEffectType.ABSORPTION)
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerTeleportEvent::class.java)
            .filter { !pearlDamage.value }
            .handler {
                it.isCancelled = true
                it.player.noDamageTicks = 1
                it.player.teleport(it.to)
            }
            .bindWith(plugin)

        Events
            .subscribe(CraftItemEvent::class.java)
            .handler {
                if (it.whoClicked is Player)
                {
                    val player = it.whoClicked
                    val item = it.currentItem
                        ?: return@handler

                    if (!godApples.value)
                    {
                        if (item.type == Material.GOLDEN_APPLE && item.durability.toInt() == 1)
                        {
                            it.isCancelled = true
                            player.sendMessage("${CC.RED}God Apples are disabled during this game!")
                        }
                    }

                    if (!goldenHeads.value)
                    {
                        if (item.type == Material.GOLDEN_APPLE && item.itemMeta.displayName.equals(
                                CC.GOLD + "Golden Head", ignoreCase = true)
                        )
                        {
                            it.isCancelled = true
                            player.sendMessage("${CC.GOLD}Golden Heads${CC.RED} are disabled during this game!")
                        }
                    }
                }
            }
            .bindWith(plugin)
    }
}
