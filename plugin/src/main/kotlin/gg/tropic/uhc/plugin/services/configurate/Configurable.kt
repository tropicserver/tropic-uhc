package gg.tropic.uhc.plugin.services.configurate

import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
data class Configurable<V : Any>(
    val name: String,
    val description: String,
    val item: ItemStack,
    var value: V,
    val defaultValue: V = value,
    val acceptedValues: List<V> = emptyList()
)
