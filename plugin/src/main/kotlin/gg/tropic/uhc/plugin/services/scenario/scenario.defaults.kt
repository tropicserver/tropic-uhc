package gg.tropic.uhc.plugin.services.scenario

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.tropic.uhc.plugin.services.configurate.oresToInventory
import gg.tropic.uhc.plugin.services.map.MapGenerationService.plugin
import me.lucko.helper.Events
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.updating.UpdatingHologramEntity
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerLevelChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
fun isNotPlaying(player: Player) = !player.playing

val bloodDiamonds = object : GameScenario(
    name = "Blood Diamonds",
    icon = ItemStack(Material.DIAMOND_PICKAXE),
    description = "You lose 1/2 heart for every diamond you mine."
)
{
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (!event.player.playing)
        {
            return
        }

        if (event.block.type == Material.DIAMOND_ORE)
        {
            event.player.damage(1.0)
        }
    }
}

val bloodEnchants = object : GameScenario(
    "Blood Enchants",
    ItemStack(Material.ENCHANTMENT_TABLE),
    "You lose half a heart for every level you enchant."
)
{
    @EventHandler
    fun onPlayerLevelChange(event: PlayerLevelChangeEvent)
    {
        if (isNotPlaying(event.player))
        {
            return
        }
        event.player.damage(1.0)
    }
}

val backpacks = object : GameScenario(
    "BackPacks",
    ItemStack(Material.CHEST),
    "Use /backpack or /bp to open the team inventory."
)
{

}

val bareBones = object : GameScenario(
    "BareBones",
    ItemStack(Material.BONE),
    "All ores except for iron & coal will drop iron ingots. Each player death drops 1 diamond, 1 golden apple, 32 arrows and 2 strings. Enchanting tables, Anvils and Golden Apples cannot be crafted. Nether is disabled."
)
{
    @EventHandler
    fun onCraftItem(event: CraftItemEvent)
    {
        if (isNotPlaying(event.view.player as Player))
        {
            return
        }

        when (event.currentItem.type)
        {
            Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.GOLDEN_APPLE ->
            {
                event.isCancelled = true
                event.view.player.sendMessage("${CC.RED}You cannot craft this because BareBones is enabled!")
            }
            else -> {}
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        event.drops.add(ItemStack(Material.GOLDEN_APPLE))
        event.drops.add(ItemStack(Material.DIAMOND))
        event.drops.add(ItemStack(Material.ARROW, 32))
        event.drops.add(ItemStack(Material.STRING, 2))
    }
}

val coldWeapons = object : GameScenario(
    "Cold Weapons",
    ItemStack(Material.SNOW_BALL),
    "Fire aspect and Flame enchantments are disabled."
)
{
    @EventHandler
    fun onEnchantItem(event: EnchantItemEvent)
    {
        if (event.enchantsToAdd[Enchantment.FIRE_ASPECT] != null || event.enchantsToAdd[Enchantment.ARROW_FIRE] != null)
        {
            event.isCancelled = true
        }
    }

/*    @EventHandler
    fun onPrepareAnvilRepair(event: PrepareAnvilRepairEvent)
    {
        if (event.getResult().getEnchantments().containsKey(Enchantment.FIRE_ASPECT) || event.getResult()
                .getEnchantments().containsKey(Enchantment.ARROW_FIRE)
        )
        {
            event.setCancelled(true)
        }
    }*/

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent)
    {
        if (event.inventory.type == InventoryType.ANVIL && event.slotType == InventoryType.SlotType.RESULT)
        {
            // We're not canceling the event here because we dont want cancel enchanting item
            // We'll just remove FIRE enchantments.
            if (event.currentItem.enchantments.containsKey(Enchantment.FIRE_ASPECT))
            {
                event.currentItem.removeEnchantment(Enchantment.FIRE_ASPECT)
            }
            if (event.currentItem.enchantments.containsKey(Enchantment.ARROW_FIRE))
            {
                event.currentItem.removeEnchantment(Enchantment.ARROW_FIRE)
            }
            (event.whoClicked as Player).updateInventory()
            if (event.currentItem.type == Material.ENCHANTED_BOOK)
            {
                if ((event.currentItem.itemMeta as EnchantmentStorageMeta).storedEnchants.containsKey(Enchantment.FIRE_ASPECT))
                {
                    event.isCancelled = true
                    event.currentItem.removeEnchantment(Enchantment.FIRE_ASPECT)
                    event.whoClicked.closeInventory()
                }
                if ((event.currentItem.itemMeta as EnchantmentStorageMeta).storedEnchants.containsKey(Enchantment.ARROW_FIRE))
                {
                    event.isCancelled = true
                    event.currentItem.removeEnchantment(Enchantment.ARROW_FIRE)
                    event.whoClicked.closeInventory()
                }
            }
        }
    }
}

val limitations = object : GameScenario(
    "Limitations",
    ItemStack(Material.LEASH),
    "You can only mine 16 diamonds, 32 gold and 64 iron."
)
{

}

val noEnchants = object : GameScenario(
    "No Enchants",
    ItemStack(Material.ENCHANTMENT_TABLE),
    "You cannot enchant or use anvils."
)
{
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.item != null && event.item.itemMeta.hasEnchants())
        {
            if (isNotPlaying(event.player))
            {
                return
            }
            event.player.itemInHand = null
            event.player.updateInventory()
            event.player.sendMessage("${CC.RED}You cannot use enchanted items this game!")
        }
    }

    @EventHandler
    fun onEnchantItem(event: EnchantItemEvent)
    {
        event.isCancelled = true
        event.enchanter.sendMessage("${CC.RED}You cannot use enchanted items this game!")
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent)
    {
        if (event.isCancelled)
        {
            return
        }
        if (isNotPlaying((event.whoClicked as Player)))
        {
            return
        }
        if (event.inventory.type == InventoryType.ANVIL && event.slotType == SlotType.RESULT)
        {
            event.isCancelled = true
            event.whoClicked.sendMessage("${CC.RED}You cannot use enchanted items this game!")
        }
    }
}

val noFallDamage = object : GameScenario(
    "NoFallDamage",
    ItemStack(Material.DIAMOND_BOOTS),
    "You cannot take fall damage."
)
{
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent)
    {
        if (event.entity is Player)
        {
            if (isNotPlaying((event.entity as Player)))
            {
                return
            }
            if (event.cause == DamageCause.FALL)
            {
                event.isCancelled = true
            }
        }
    }
}

val horseLess = object : GameScenario(
    "Horseless",
    ItemStack(Material.DIAMOND_BARDING),
    "You cannot tame horses. You cannot tame donkeys."
)
{
    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent)
    {
        if (event.rightClicked.type == EntityType.HORSE)
        {
            event.isCancelled = true
        }
    }
}

val cutClean = object : GameScenario(
    "CutClean",
    ItemStack(Material.IRON_INGOT),
    "All ores and food are automatically cooked or smelted."
)
{
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (event.isCancelled || isNotPlaying(event.player))
        {
            return
        }

        if (oreFrenzy.enabled)
        {
            when (event.block.type)
            {
                Material.LAPIS_ORE ->
                {
                    event.isCancelled = true
                    event.block.type = Material.AIR
                    handleFrenzy(event.block, event.player, ItemStack(Material.POTION, 1, 16453.toShort()))
                }

                Material.EMERALD_ORE ->
                {
                    event.isCancelled = true
                    event.block.type = Material.AIR
                    handleFrenzy(event.block, event.player, ItemStack(Material.ARROW, 32))
                }

                Material.REDSTONE_ORE ->
                {
                    event.isCancelled = true
                    event.block.type = Material.AIR
                    handleFrenzy(event.block, event.player, ItemStack(Material.BOOK))
                }

                Material.QUARTZ_ORE ->
                {
                    event.isCancelled = true
                    event.block.type = Material.AIR
                    handleFrenzy(event.block, event.player, ItemStack(Material.TNT))
                }

                else -> {}
            }
        }
        when (event.block.type)
        {
            Material.DIAMOND_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.GRAVEL ->
            {
                event.isCancelled = true
                handleSmelt(event.block, event.player)
            }
            else -> {}
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent)
    {
        val dropOther = if (tripleOres.enabled) 6 else if (doubleOres.enabled) 3 else 2
        val dropRare = if (tripleOres.enabled) 3 else if (doubleOres.enabled) 2 else 1

        when (event.entityType)
        {
            EntityType.COW ->
            {
                event.drops.clear()
                event.drops.add(ItemStack(Material.COOKED_BEEF, dropOther))
                event.drops.add(ItemStack(Material.LEATHER, dropRare))
            }

            EntityType.CHICKEN ->
            {
                event.drops.clear()
                event.drops.add(ItemStack(Material.FEATHER, dropRare))
                event.drops.add(ItemStack(Material.COOKED_CHICKEN, dropOther))
            }

            EntityType.HORSE ->
            {
                event.drops.clear()
                event.drops.add(ItemStack(Material.LEATHER, dropRare))
            }

            EntityType.PIG ->
            {
                event.drops.clear()
                event.drops.add(ItemStack(Material.GRILLED_PORK, dropRare))
            }

            else -> {}
        }
    }

    private fun handleSmelt(block: Block, player: Player)
    {
        // Type must be here because if we get type after setType(Material.AIR) then we won't be able
        // to get material type because its AIR
        val type =
            if (block.type == Material.DIAMOND_ORE) if (bareBones.enabled) Material.IRON_INGOT else Material.DIAMOND else if (block.type == Material.COAL_ORE) Material.COAL else if (block.type == Material.IRON_ORE) Material.IRON_INGOT else if (block.type == Material.GOLD_ORE) if (bareBones.enabled
            ) Material.IRON_INGOT else Material.GOLD_INGOT else if (block.type == Material.GRAVEL) Material.FLINT else Material.AIR
        block.type = Material.AIR
        block.state.update()

        // We're not letting flint drop exp lmao
        if (type != Material.FLINT)
        {
            // 9 - 6 - 3
            block.world.spawn(block.location, ExperienceOrb::class.java).experience =
                if (tripleExp.enabled) 6 else if (doubleExp.enabled) 4 else 2
        }

        // We'll still drop exp soo people dont complain
        if (goldless.enabled && type == Material.GOLD_INGOT)
        {
            return
        }

        if (diamondless.enabled && type == Material.DIAMOND)
        {
            return
        }

        if (limitations.enabled)
        {
            val data = player.profile

            when (type)
            {
                Material.DIAMOND ->
                {
                    if (data.limDiamond == 16)
                    {
                        player.sendMessage(CC.RED + "You cannot mine any more diamonds.")
                        return
                    }

                    data.limDiamond += 1
                }

                Material.GOLD_INGOT ->
                {
                    if (data.limGold == 32)
                    {
                        player.sendMessage(CC.RED + "You cannot mine any more gold.")
                        return
                    }

                    data.limGold += 1
                }

                Material.IRON_INGOT ->
                {
                    if (data.limIron == 64)
                    {
                        player.sendMessage(CC.RED + "You cannot mine any more iron.")
                        return
                    }

                    data.limIron += 1
                }

                else -> {}
            }
        }

        val blockDropAmount = if (tripleOres.enabled) 3 else if (doubleOres.enabled) 2 else 1

        if (oresToInventory.value)
        {
            // If inventory is full then drop naturally
            if (player.inventory.firstEmpty() == -1)
            {
                block.world.dropItemNaturally(block.location, ItemStack(type, blockDropAmount))
                if (oreFrenzy.enabled && type == Material.DIAMOND)
                {
                    block.world.dropItemNaturally(block.location, ItemStack(Material.EXP_BOTTLE, 4))
                }
                return
            }
            player.inventory.addItem(ItemStack(type, blockDropAmount))
            if (oreFrenzy.enabled && type == Material.DIAMOND)
            {
                player.inventory.addItem(ItemStack(Material.EXP_BOTTLE, 4))
            }
        } else
        {
            block.world.dropItemNaturally(block.location, ItemStack(type, blockDropAmount))
            if (oreFrenzy.enabled && type == Material.DIAMOND)
            {
                block.world.dropItemNaturally(block.location, ItemStack(Material.EXP_BOTTLE, 4))
            }
        }
    }

    private fun handleFrenzy(block: Block, player: Player, stack: ItemStack)
    {
        if (oresToInventory.value)
        {
            // If inventory is full then drop naturally
            if (player.inventory.firstEmpty() == -1)
            {
                block.world.dropItemNaturally(block.location, stack)
                return
            }
            player.inventory.addItem(stack)
        } else
        {
            block.world.dropItemNaturally(block.location, stack)
        }
    }
}

val oreFrenzy = object : GameScenario(
    "Ore Frenzy", ItemStack(Material.LAPIS_ORE),
    "When you mine lapis ore it drops a health splash potion." +
            " When you mine emerald ore it drops 32 arrows." +
            " When you mine redstone ore it drops an unenchanted book. " +
            "When you mine diamond ore it drops a diamond and 4 bottles of experience. " +
            "When you mine quartz ore it drops a block of TNT."
)
{

}

val luckyLeaves = object : GameScenario(
    "Lucky Leaves",
    ItemStack(Material.LEAVES),
    "There is a low chance of golden apples dropping from trees."
)
{
    @EventHandler
    fun onLeavesDecay(event: LeavesDecayEvent)
    {
        if (CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED)
        {
            if (Math.random() * 100 <= 0.75)
            {
                event.block.world.dropItemNaturally(event.block.location, ItemStack(Material.GOLDEN_APPLE))
            }
        }
    }
}

val hasteyBoys = object : GameScenario(
    "Hastey Boys",
    ItemStack(Material.IRON_PICKAXE),
    "Everyone mines faster."
)
{
    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent)
    {
        val material = event.inventory.result.type

        if (material.name.endsWith("_PICKAXE")
            || material.name.endsWith("_SPADE")
            || material.name.endsWith("_AXE")
            || material.name.endsWith("_HOE")
        )
        {
            event.inventory.result = ItemBuilder(material)
                .enchant(Enchantment.DIG_SPEED, 3)
                .enchant(Enchantment.DURABILITY, 3)
                .build()
        }
    }
}

val diamondless = object : GameScenario(
    "Diamondless",
    ItemStack(Material.DIAMOND),
    "You cannot mine diamonds. Players drop 1 diamond on death."
)
{
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        event.drops.add(ItemStack(Material.DIAMOND))
    }
}

val goldless = object : GameScenario(
    "Goldless",
    ItemStack(Material.GOLD_INGOT),
    "You cannot mine gold. Players drop 8 gold ingot and 1 golden head on death."
)
{
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        event.drops.add(goldenHead())
        event.drops.add(ItemStack(Material.GOLD_INGOT, 8))
    }
}

val goldenRetriever = object : GameScenario(
    "Golden Retriever",
    ItemStack(Material.GOLDEN_APPLE),
    "Players drop 1 golden head on death."
)
{
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        if (CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED)
        {
            event.drops.add(goldenHead())
        }
    }
}

val timeBomb = object : GameScenario(
    "Time Bomb",
    ItemStack(Material.TNT),
    "When a player dies, their loot will drop into a chest. After 30s, the chest will explode."
)
{
    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent)
    {
        event.blockList().removeIf { block: Block -> block.type == Material.BEDROCK }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeathEvent(event: PlayerDeathEvent)
    {
        handleTimeBomb(event.entity, event.drops, listOf())
    }

    fun handleTimeBomb(
        entity: Entity,
        drops: MutableList<ItemStack?>?,
        items: List<ItemStack?>
    )
    {
        drops?.clear()
        val where = entity.location
        where.block.type = Material.CHEST
        val chest = where.block.state as Chest
        where.add(1.0, 0.0, 0.0).block.type = Material.CHEST
        where.add(0.0, 1.0, 0.0).block.type = Material.AIR
        where.add(1.0, 1.0, 0.0).block.type = Material.AIR
        val secondChest: Chest
        secondChest = try
        {
            chest.location.add(1.0, 0.0, 0.0).block.state as Chest
        } catch (e: Exception)
        {
            chest
        }
        if (entity is Player)
        {
            val player = entity
            val killer = player.killer

            if (killer != null)
            {
                val explosionTime = System.currentTimeMillis() + 1000L * 20
                val hologram = object : UpdatingHologramEntity(
                    text = "${CC.GREEN}${entity.name}'s corpse",
                    location = chest.location.clone().add(1.0, 1.8, 0.5)
                )
                {
                    override fun getNewLines() = listOf(
                        "${CC.GREEN}${entity.name}'s corpse",
                        "${CC.SEC}Explodes in ${CC.PRI}${
                            DurationFormatUtils.formatDurationWords(explosionTime - System.currentTimeMillis(), true, true)
                        }"
                    )

                    override fun getTickInterval() = 15L
                }

                hologram.initializeData()
                EntityHandler.trackEntity(hologram)

                val terminable = CompositeTerminable.create()

                Events
                    .subscribe(BlockBreakEvent::class.java)
                    .filter {
                        it.block.location.equals(chest.location) || it.block.location.equals(secondChest.location)
                    }
                    .handler {
                        it.isCancelled = true
                    }
                    .bindWith(terminable)

                Bukkit.getScheduler().runTaskLater(plugin, {
                    hologram.destroyForCurrentWatchers()
                    EntityHandler.forgetEntity(hologram)

                    terminable.closeAndReportException()

                    where.world.spigot().strikeLightning(where, true)
                    where.world.createExplosion(where, 8f)

                    Bukkit.broadcastMessage("${CC.GREEN}${entity.name}'s${CC.SEC} corpse exploded!")
                }, 30L * 20)
            }
        }

        // Should never happend but yea
        items.stream().filter { stack: ItemStack? -> stack != null && stack.type != Material.AIR }
            .forEach { stack: ItemStack? ->
                chest.inventory.addItem(stack)
            }

        // always adding 1 head
        chest.inventory.addItem(goldenHead())
        if (goldenRetriever.enabled)
        {
            chest.inventory.addItem(goldenHead())
        }
        if (diamondless.enabled)
        {
            chest.inventory.addItem(ItemStack(Material.DIAMOND, 1))
        }
        if (goldless.enabled)
        {
            chest.inventory.addItem(ItemStack(Material.GOLD_INGOT, 8))
            chest.inventory.addItem(goldenHead())
        }
        if (bareBones.enabled)
        {
            chest.inventory.addItem(ItemStack(Material.GOLDEN_APPLE))
            chest.inventory.addItem(ItemStack(Material.DIAMOND))
            chest.inventory.addItem(ItemStack(Material.ARROW, 32))
            chest.inventory.addItem(ItemStack(Material.STRING, 2))
        }
    }
}

val fireless = object : GameScenario(
    "Fireless",
    ItemStack(Material.FLINT_AND_STEEL),
    "You cannot take fire damage only in overworld."
)
{
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent)
    {
        if (event.entity is Player)
        {
            if (isNotPlaying((event.entity as Player)) || event.entity.world.name == "uhc_nether")
            {
                return
            }
            if (event.cause == DamageCause.FIRE || event.cause == DamageCause.FIRE_TICK || event.cause == DamageCause.LAVA)
            {
                event.isCancelled = true
            }
        }
    }
}

val doubleExp = object : GameScenario(
    "Double Exp",
    ItemStack(Material.EXP_BOTTLE, 2),
    "When you mine ores you receive double exp."
)
{

}

val rodLess = object : GameScenario(
    "Rodless",
    ItemStack(Material.FISHING_ROD),
    "Fishing Rods cannot be crafted/used."
)
{
    @EventHandler
    fun onCraftItem(event: CraftItemEvent)
    {
        if (isNotPlaying((event.view.player as Player)))
        {
            return
        }
        if (event.recipe.result.type == Material.FISHING_ROD)
        {
            event.inventory.result = ItemStack(Material.AIR)
            event.view.player.sendMessage("${CC.RED}You cannot use fishing rods this game!")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.item != null && event.item.type == Material.FISHING_ROD)
        {
            if (isNotPlaying(event.player))
            {
                return
            }
            event.player.itemInHand = null
            event.player.updateInventory()
            event.player.sendMessage("${CC.RED}You cannot use fishing rods this game!")
        }
    }

}

val swordLess = object : GameScenario(
    "Swordless",
    ItemStack(Material.GOLD_SWORD),
    "Swords cannot be crafted/used."
)
{
    @EventHandler
    fun onCraftItem(event: CraftItemEvent)
    {
        if (isNotPlaying((event.view.player as Player)))
        {
            return
        }
        if (event.recipe.result.type.name.endsWith("_SWORD"))
        {
            event.inventory.result = ItemStack(Material.AIR)
            event.view.player.sendMessage("${CC.RED}You cannot use swords during this game!")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.item != null && event.item.type.name.endsWith("_SWORD"))
        {
            if (isNotPlaying(event.player))
            {
                return
            }
            event.player.itemInHand = null
            event.player.updateInventory()
            event.player.sendMessage("${CC.RED}You cannot use swords during this game!")
        }
    }
}

val timber = object : GameScenario(
    "Timber",
    ItemStack(Material.LOG),
    "When you break one log, it automatically breaks whole tree."
)
{
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (isNotPlaying(event.player))
        {
            return
        }

        if (event.block.type == Material.LOG || event.block.type == Material.LOG_2)
        {
            event.isCancelled = true
            var up = event.block.getRelative(BlockFace.UP)
            var down = event.block.getRelative(BlockFace.DOWN)
            event.block.type = Material.AIR
            event.block.state.update()
            while (up.type == Material.LOG || up.type == Material.LOG_2)
            {
                if (event.player.inventory.firstEmpty() == -1)
                {
                    up.breakNaturally()
                } else
                {
                    if (up.type == Material.LOG || up.type == Material.LOG_2)
                    {
                        up.drops.clear()
                        event.player.inventory.addItem(ItemStack(Material.LOG, 1, up.data.toShort()))
                        up.type = Material.AIR
                    }
                }
                up = up.getRelative(BlockFace.UP)
            }
            while (down.type == Material.LOG || down.type == Material.LOG_2)
            {
                if (event.player.inventory.firstEmpty() == -1)
                {
                    down.breakNaturally()
                } else
                {
                    if (down.type == Material.LOG || down.type == Material.LOG_2)
                    {
                        down.drops.clear()
                        event.player.inventory.addItem(ItemStack(Material.LOG, 1, down.data.toShort()))
                        down.type = Material.AIR
                    }
                }
                down = down.getRelative(BlockFace.DOWN)
            }
        }
    }
}

val doubleOres = object : GameScenario(
    "Double Ores",
    ItemStack(Material.GOLD_INGOT, 2),
    "All ores and food are doubled when mined/harvested."
)
{

}

val webCage = object : GameScenario(
    "WebCage",
    ItemStack(Material.WEB),
    "When you kill a player a sphere of cobwebs surrounds you."
)
{
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        if (isNotPlaying(event.entity))
        {
            return
        }
        if (event.entity.killer == null)
        {
            return
        }
        val locations = getSphere(event.entity.location)
        for (blocks in locations)
        {
            if (blocks.block.type == Material.AIR)
            {
                blocks.block.type = Material.WEB
            }
        }
    }

    private fun getSphere(centerBlock: Location): List<Location>
    {
        val circleBlocks: MutableList<Location> = ArrayList()
        val bx = centerBlock.blockX
        val by = centerBlock.blockY
        val bz = centerBlock.blockZ
        for (x in bx - 5..bx + 5)
        {
            for (y in by - 5..by + 5)
            {
                for (z in bz - 5..bz + 5)
                {
                    val distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y)).toDouble()
                    if (distance < 5 * 5 && !(distance < (5 - 1) * (5 - 1)))
                    {
                        val l = Location(centerBlock.world, x.toDouble(), y.toDouble(), z.toDouble())
                        circleBlocks.add(l)
                    }
                }
            }
        }
        return circleBlocks
    }
}

val tripleOres = object : GameScenario(
    "Triple Ores",
    ItemStack(Material.DIAMOND, 3),
    "All ores and food are tripled when mined/harvested."
)
{

}

val tripleExp = object : GameScenario(
    "Triple Exp",
    ItemStack(Material.EXP_BOTTLE, 3),
    "When you mine ores you receive triple exp."
)
{

}

val bowless = object : GameScenario(
    "Bowless",
    ItemStack(Material.BOW),
    "Bows cannot be crafted/used."
)
{
    @EventHandler
    fun onCraftItem(event: CraftItemEvent)
    {
        if (isNotPlaying((event.view.player as Player)))
        {
            return
        }

        if (event.recipe.result.type == Material.BOW)
        {
            event.inventory.result = ItemStack(Material.AIR)
            event.view.player.sendMessage("${CC.RED}You cannot use bows during this game!")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if (event.item != null && event.item.type == Material.BOW)
        {
            if (isNotPlaying(event.player))
            {
                return
            }

            event.player.itemInHand = null
            event.player.updateInventory()
            event.player.sendMessage("${CC.RED}You cannot use bows during this game!")
        }
    }
}
