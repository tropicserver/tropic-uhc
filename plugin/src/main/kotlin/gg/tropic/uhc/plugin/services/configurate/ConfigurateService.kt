package gg.tropic.uhc.plugin.services.configurate

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.scenario.GoldenHead
import gg.tropic.uhc.plugin.services.scenario.playing
import gg.tropic.uhc.plugin.services.scenario.profile
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*


/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object ConfigurateService
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    var customGoldenHeadLogic = { player: Player -> }

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerInteractEvent::class.java)
            .handler { event ->
                val player = event.player
                val stack = player.itemInHand

                if (stack.type == Material.POTION)
                {
                    if (!invisibilityPotions.value)
                    {
                        when (stack.durability.toInt())
                        {
                            16462, 16430, 8270, 8238 ->
                            {
                                event.isCancelled = true
                                event.player
                                    .sendMessage("${CC.RED}Invis pots are disabled this game!")
                                event.player.itemInHand.durability = 0.toShort()
                            }
                        }
                    }

                    if (!speedPotions.value)
                    {
                        when (stack.durability.toInt())
                        {
                            16450, 16418, 16386, 8258, 8226, 8194 ->
                            {
                                event.isCancelled = true
                                event.player
                                    .sendMessage("${CC.RED}Speed pots are disabled this game!")
                                event.player.itemInHand.durability = 0.toShort()
                            }
                        }
                    }

                    if (!strengthPotions.value)
                    {
                        when (stack.durability.toInt())
                        {
                            16457, 16425, 16393, 8265, 8233, 8201 ->
                            {
                                event.isCancelled = true
                                event.player
                                    .sendMessage("${CC.RED}Strength pots are disabled this game!")
                                stack.durability = 0.toShort()
                            }
                        }
                    }
                }

                if (!iPvP.value)
                {
                    if (event.action !== Action.RIGHT_CLICK_BLOCK || event.item == null || event.item
                            .type !== Material.FLINT_AND_STEEL || mapWorld().pvp
                    )
                    {
                        return@handler
                    }

                    event.player
                        .getNearbyEntities(5.0, 5.0, 5.0)
                        .filterIsInstance<Player>()
                        .forEach { _ ->
                            event.isCancelled = true
                            event.player
                                .sendMessage("${CC.RED}iPvP is not allowed this game!")
                        }
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerPortalEvent::class.java)
            .filter { !nether.value }
            .handler {
                it.isCancelled = true
                it.player.sendMessage("${CC.RED}Nether is disabled during this game!")
            }
            .bindWith(plugin)

        Events
            .subscribe(EntityDamageEvent::class.java)
            .filter { !iPvP.value }
            .handler { event ->
                if (
                    event.entity is Player &&
                    event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION &&
                    !mapWorld().pvp
                )
                {
                    event.entity.sendMessage("${CC.RED}iPvP is not allowed this game!")
                    event.isCancelled = true
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerBucketEmptyEvent::class.java)
            .filter { !iPvP.value }
            .handler { event ->
                if (!mapWorld().pvp)
                {
                    event.player.getNearbyEntities(5.0, 5.0, 5.0)
                        .filterIsInstance<Player>()
                        .forEach { _ ->
                            if (event.bucket == Material.LAVA_BUCKET)
                            {
                                event.isCancelled = true
                                event.player.sendMessage("${CC.RED}iPvP is not allowed this game!")
                            }
                        }
                }
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
            .subscribe(BlockBreakEvent::class.java)
            .handler {
                if (it.isCancelled || it.player.gameMode == GameMode.CREATIVE || !it.player.playing || it.player
                        .itemInHand.type != Material.SHEARS
                )
                {
                    return@handler
                }

                if (it.block.type == Material.LEAVES || it.block.type == Material.LEAVES_2)
                {
                    if ((0..100).random() <= shearsRate.value)
                    {
                        it.block.world.dropItemNaturally(
                            it.block.location, ItemStack(Material.APPLE)
                        )
                    }
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(LeavesDecayEvent::class.java)
            .handler {
                if (CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED)
                {
                    if ((0..100).random() <= decayDropRate.value)
                    {
                        it.block.world.dropItemNaturally(
                            it.block.location, ItemStack(Material.APPLE)
                        )
                    }
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

        val headCooldown = mutableMapOf<UUID, Boolean>()

        Events
            .subscribe(PlayerInteractEvent::class.java)
            .handler { event ->
                if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return@handler

                val player = event.player
                val hand = player.itemInHand

                if (headCooldown.getOrDefault(player.uniqueId, false))
                {
                    player.sendMessage(ChatColor.RED.toString() + "Wait 2 seconds before eating a head again.")
                    return@handler
                }

                // Ignore items that are golden heads or non-heads
                // Note: Golden Heads are handled in GoldenHeadCraft.java
                if (GoldenHead.isGoldenHead(hand)) return@handler
                if (!GoldenHead.isHead(hand)) return@handler

                event.isCancelled = true

                // Decrement amount of heads they're holding
                if (hand.amount <= 1)
                {
                    player.itemInHand = ItemStack(Material.AIR)
                } else
                {
                    hand.amount = hand.amount - 1
                }

                //Eat Sound
                player.playSound(player.location, Sound.EAT, 10f, 1f)

                // Add 1 heart to the player
                player.health = player.maxHealth.coerceAtMost(player.health + 1)

                fun overridePotionEffect(player: Player, effect: PotionEffect)
                {
                    if (player.hasPotionEffect(effect.type))
                    {
                        player.removePotionEffect(effect.type)
                    }

                    player.addPotionEffect(effect)
                }

                // Give regen effect
                overridePotionEffect(player, PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 2))
                customGoldenHeadLogic.invoke(player)

                headCooldown.put(player.uniqueId, false)

                object : BukkitRunnable()
                {
                    override fun run()
                    {
                        headCooldown.put(player.uniqueId, false)
                    }
                }.runTaskLater(plugin, (2 * 20).toLong())
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerPortalEvent::class.java)
            .handler { event ->
                if (event.isCancelled)
                {
                    return@handler
                }

                val player: Player = event.player

                if (!nether.value || ((System.currentTimeMillis() - CgsGameEngine.INSTANCE.gameStart) / 1000L) < gracePeriod.value * 60)
                {
                    event.isCancelled = true
                    player.sendMessage(if (nether.value) "${CC.RED}Nether is disabled this game!" else CC.RED + "You can enter nether after ${gracePeriod.value} minutes.")
                    return@handler
                }

                if (WorldBorderService.currentSize <= 500)
                {
                    event.isCancelled = true
                    player.sendMessage(CC.RED + "You cannot enter nether while the border is under 500.")
                    return@handler
                }

                if (!event.isCancelled && event.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)
                {
                    event.portalTravelAgent.setSearchRadius(32)

                    if (event.from.world.name.equals("uhc_world", ignoreCase = true))
                    {
                        val x = player.location.x / 8.0
                        val y = player.location.y
                        val z = player.location.z / 8.0
                        event.to =
                            event.portalTravelAgent.findOrCreate(Location(Bukkit.getWorld("uhc_nether"), x, y, z))
                    } else if (event.from.world.name.equals("uhc_nether", ignoreCase = true))
                    {
                        val x = player.location.x * 8.0
                        val y = player.location.y
                        val z = player.location.z * 8.0
                        event.to = event.portalTravelAgent.findOrCreate(Location(Bukkit.getWorld("uhc_world"), x, y, z))
                    }
                }
            }
            .bindWith(plugin)

        Events
            .subscribe(BlockBreakEvent::class.java)
            .handler { event ->
                if (!event.isCancelled)
                {
                    val profile = event.player.profile

                    if (event.player.playing)
                    {
                        when (event.block.type)
                        {
                            Material.DIAMOND_ORE -> profile.diamondsMined.inc()
                            Material.GOLD_ORE -> profile.goldMined.inc()
                            Material.COAL_ORE -> profile.coalMined.inc()
                            Material.IRON_ORE -> profile.ironMined.inc()
                            Material.REDSTONE_ORE -> profile.redstoneMined.inc()
                            Material.MOB_SPAWNER -> profile.spawnersMined.inc()
                            Material.LAPIS_ORE -> profile.lapisMined.inc()

                            else ->
                            {
                            }
                        }
                    }

                    if (
                        Bukkit.getOnlinePlayers().size < 350 ||
                        event.player.gameMode != GameMode.CREATIVE
                    )
                    {
                        when (event.block.type)
                        {
                            Material.MOB_SPAWNER, Material.DIAMOND_ORE -> for (p in Bukkit.getOnlinePlayers())
                            {
                                if (!p.playing && p.hasPermission("uhc.xray-alerts") && p.hasMetadata("xray-alerts"))
                                {
                                    FancyMessage()
                                        .withMessage(
                                            (((((((CC.GRAY + "[" + CC.B_RED).toString() + "⚠" + CC.GRAY).toString() + "] "
                                                    + event.player.displayName
                                                    + CC.GRAY).toString() + " found " + CC.RESET
                                                    ).toString() + (if (event.block.type == Material.DIAMOND_ORE) "Diamond Ore" else "Mob Spawner")
                                                    + CC.GRAY).toString() + ". " + CC.RED).toString() + "(" + if (event.block.type == Material.DIAMOND_ORE) profile.diamondsMined.value else profile.spawnersMined.value
                                                    ).toString() + ")"
                                        )
                                        .andHoverOf("${CC.GREEN}Click to tp!")
                                        .andCommandOf(ClickEvent.Action.RUN_COMMAND, "/tp ${event.player.name}")
                                        .sendToPlayer(p)
                                }
                            }

                            else ->
                            {
                            }
                        }
                    }
                }
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
                                CC.GOLD + "Golden Head", ignoreCase = true
                            )
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
