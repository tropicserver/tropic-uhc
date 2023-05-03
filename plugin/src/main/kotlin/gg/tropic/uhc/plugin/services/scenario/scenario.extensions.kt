package gg.tropic.uhc.plugin.services.scenario

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.tropic.uhc.plugin.engine.CountdownRunnable
import gg.tropic.uhc.shared.player.UHCPlayerModel
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta


/**
 * @author GrowlyX
 * @since 4/22/2023
 */
val Player.playing: Boolean
    get() = !hasMetadata("spectator")

val Player.activeNoClean: Pair<Long, CountdownRunnable>?
    get() = noCleanUsers[uniqueId]

val Player.profile: UHCPlayerModel
    get() = CgsGameEngine.INSTANCE
        .getStatistics(
            CgsPlayerHandler.find(player)!!
        ) as UHCPlayerModel


fun goldenHead() = GoldenHead.goldenHead!!

object GoldenHead {
    var goldenHead: ItemStack? = null
    fun isHead(item: ItemStack): Boolean {
        return item.type === Material.SKULL_ITEM && item.durability.toInt() == 3
    }

    fun isGoldenHead(item: ItemStack): Boolean {
        return isHead(item) && item.hasItemMeta() && item.itemMeta.hasDisplayName() && item.itemMeta
            .displayName.equals(ChatColor.GOLD.toString() + "Golden Head")
    }

    fun getSkull(username: String?): ItemStack {
        val goldenHead = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val meta: ItemMeta = goldenHead.itemMeta
        meta.displayName = username
        val sMeta = meta as SkullMeta
        sMeta.setOwner(username)
        goldenHead.setItemMeta(sMeta)
        return goldenHead
    }

    init {
        val item: ItemStack = getSkull("michaelmmc")
        val meta = item.itemMeta
        meta.displayName = ChatColor.GOLD.toString() + "Golden Head"
        item.setItemMeta(meta)
        goldenHead = item
    }
}
