package gg.tropic.uhc.plugin.services.border.bedrock

import gg.scala.cgs.common.CgsGameEngine
import gg.tropic.uhc.plugin.services.map.mapWorld
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author GrowlyX
 * @since 4/24/2023
 */
object BedrockBorderMechanism
{
    private val blockedWallBlocks = listOf(
        Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2,
        Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA,
        Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.DOUBLE_PLANT, Material.LONG_GRASS,
        Material.VINE, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.CACTUS, Material.DEAD_BUSH,
        Material.SUGAR_CANE_BLOCK, Material.ICE, Material.SNOW
    )

    fun configureBedrockBorder(radius: Double)
    {
        addBedrockBorder(mapWorld().name, radius.toInt(), 6)
    }

    private fun addBedrockBorder(world: String, radius: Int, blocksHigh: Int)
    {
        for (i in 0 until blocksHigh)
        {
            Bukkit.getScheduler()
                .runTaskLater(CgsGameEngine.INSTANCE.plugin, { this.addBedrockBorder(world, (radius / 2)) }, i.toLong())
        }
    }

    private fun figureOutBlockToMakeBedrock(world: String, x: Int, z: Int)
    {
        val block = Bukkit.getWorld(world).getHighestBlockAt(x, z)
        var below = block.getRelative(BlockFace.DOWN)
        while (blockedWallBlocks.contains(below.type) && below.y > 1)
        {
            below = below.getRelative(BlockFace.DOWN)
        }
        below.getRelative(BlockFace.UP).type = Material.BEDROCK
    }

    private fun addBedrockBorder(world: String, radius: Int)
    {
        object : BukkitRunnable()
        {
            private var counter = -radius - 1
            private var phase1 = false
            private var phase2 = false
            private var phase3 = false
            override fun run()
            {
                if (!phase1)
                {
                    val maxCounter = counter + 500
                    val x = -radius - 1
                    var z = counter
                    while (z <= radius && counter <= maxCounter)
                    {
                        figureOutBlockToMakeBedrock(world, x, z)
                        z++
                        counter++
                    }
                    if (counter >= radius)
                    {
                        counter = -radius - 1
                        phase1 = true
                    }
                    return
                }
                if (!phase2)
                {
                    val maxCounter = counter + 500
                    var z = counter
                    while (z <= radius && counter <= maxCounter)
                    {
                        figureOutBlockToMakeBedrock(world, radius, z)
                        z++
                        counter++
                    }
                    if (counter >= radius)
                    {
                        counter = -radius - 1
                        phase2 = true
                    }
                    return
                }
                if (!phase3)
                {
                    val maxCounter = counter + 500
                    val z = -radius - 1
                    var x = counter
                    while (x <= radius && counter <= maxCounter)
                    {
                        if (x == radius || x == -radius - 1)
                        {
                            x++
                            counter++
                            continue
                        }
                        figureOutBlockToMakeBedrock(world, x, z)
                        x++
                        counter++
                    }
                    if (counter >= radius)
                    {
                        counter = -radius - 1
                        phase3 = true
                    }
                    return
                }
                val maxCounter = counter + 500
                var x = counter
                while (x <= radius && counter <= maxCounter)
                {
                    if (x == radius || x == -radius - 1)
                    {
                        x++
                        counter++
                        continue
                    }
                    figureOutBlockToMakeBedrock(world, x, radius)
                    x++
                    counter++
                }
                if (counter >= radius)
                {
                    cancel()
                }
            }
        }.runTaskTimer(CgsGameEngine.INSTANCE.plugin, 0L, 5L)
    }
}
