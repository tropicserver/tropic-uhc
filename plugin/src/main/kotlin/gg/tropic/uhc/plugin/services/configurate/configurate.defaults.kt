package gg.tropic.uhc.plugin.services.configurate

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
val configurables = mutableListOf<Configurable<*>>()

inline fun <reified V : Any> configurable(
    name: String,
    item: ItemStack,
    description: String,
    value: V,
    vararg acceptedValues: V
) = Configurable(
    name, description, item, value,
    acceptedValues = mutableSetOf<V>()
        .apply {
            if (acceptedValues.isEmpty())
            {
                return@apply
            }

            add(value)
            addAll(acceptedValues.toList())
        }
).apply {
    configurables.add(this)
}

val absorption = configurable(
    "Absorption",
    ItemStack(Material.GOLDEN_CARROT),
    "Should Absorption be enabled when data eat golden apples?",
    true
)

val borderShrink = configurable(
    "Border Shrink Interval",
    ItemStack(Material.PAPER),
    "How often does the world border shrink. (in minutes)",
    1,
    3,
    5,
    10,
    15
)

val initialBorderSize = configurable(
    "Border Initial Size",
    ItemStack(Material.SIGN),
    "What should the initial border size be?",
    500
)

val borderDecreaseAmount = configurable(
    "Border Decrease Amount",
    ItemStack(Material.HOPPER),
    "How much should we decrease the border by?",
    50
)

val finalHeal = configurable(
    "Final Heal",
    ItemStack(Material.SPECKLED_MELON),
    "What time should all players be healed? (in minutes)",
    1,
    5,
    10,
    15,
    20,
    25
)

val firstShrink = configurable(
    "First Shrink",
    ItemStack(Material.BEDROCK),
    "What time should the first border shrink. (in minutes)",
    9,
    1,
    5,
    10,
    15,
    20,
    25,
    30,
    35,
    40,
    45,
    50,
    55,
    60
)

val godApples = configurable(
    "God Apples",
    ItemStack(Material.GOLDEN_APPLE, 1, 1.toShort()),
    "Should god apples be enabled?",
    false
)

val goldenHeads = configurable(
    "Golden Heads",
    ItemStack(Material.GOLDEN_APPLE),
    "Should golden heads be enabled?",
    true
)

val invisibilityPotions = configurable(
    "Invisibility Potions",
    ItemStack(Material.POTION, 1, 8238.toShort()),
    "Should players be able to brew invisibility potions at all?",
    true
)

val nether = configurable(
    "Nether",
    ItemStack(Material.NETHERRACK),
    "Should the nether be enabled?",
    true
)

val pearlDamage = configurable(
    "Ender Pearl Damage",
    ItemStack(Material.ENDER_PEARL),
    "Should players take damage when an ender pearl lands?",
    true
)

val gracePeriod = configurable(
    "Grace Period Duration",
    ItemStack(Material.DIAMOND_SWORD),
    "How long should the PvP Period last? (in minutes)",
    3,
    5,
    10,
    15,
    20,
    25,
    30,
    35,
    45,
    50,
    55,
    60
)

val speedPotions = configurable(
    "Speed Potions",
    ItemStack(Material.POTION, 1, 8194.toShort()),
    "Should players be able to brew speed potions at all?",
    true
)

val starterFood = configurable(
    "Starter Food",
    ItemStack(Material.COOKED_BEEF),
    "How much steak should players receive on uhc start?",
    0,
    10,
    16,
    32,
    64
)

val flatMeetup = configurable(
    "Flat 25x25",
    ItemStack(Material.GRASS),
    "Should game have flat at 25x25 border?",
    false
)

val iPvP = configurable(
    "iPvP",
    ItemStack(Material.WATCH),
    "Should iPvP be enabled?",
    false
)

val oresToInventory = configurable(
    "Ores to inventory",
    ItemStack(Material.DIAMOND_ORE),
    "Should ores go to player inventory?",
    false
)

val netherTime = configurable(
    "Nether Time",
    ItemStack(Material.NETHER_FENCE),
    "Should players be able to go to nether before 20 minutes?",
    false
)


val shearsRate = configurable(
    "Apple Drop Rate",
    ItemStack(Material.SHEARS),
    "%age chance for apples to drop when you break leaves.",
    1
)

val decayDropRate = configurable(
    "Apple Leaves Drop Rate",
    ItemStack(Material.LEAVES_2),
    "%age chance for apples to drop when you leaves decay.",
    3
)
