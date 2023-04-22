package gg.tropic.uhc.plugin.services.map.biome

import net.minecraft.server.v1_8_R3.BiomeBase
import org.bukkit.block.Biome
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
object AlternativeBiomeSwapper
{
    fun swap()
    {
        setBiomeBase(Biome.SMALL_MOUNTAINS, Biome.SAVANNA, 0)
        setBiomeBase(Biome.MUSHROOM_ISLAND, Biome.SAVANNA, 0)
        setBiomeBase(Biome.MUSHROOM_SHORE, Biome.SAVANNA, 0)
        setBiomeBase(Biome.DESERT_MOUNTAINS, Biome.DESERT, 0)
        setBiomeBase(Biome.DESERT_HILLS, Biome.DESERT, 0)
        setBiomeBase(Biome.FLOWER_FOREST, Biome.PLAINS, 0)
        setBiomeBase(Biome.SUNFLOWER_PLAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.OCEAN, Biome.PLAINS, 0)
        setBiomeBase(Biome.RIVER, Biome.PLAINS, 0)
        setBiomeBase(Biome.BEACH, Biome.TAIGA, 0)
        setBiomeBase(Biome.JUNGLE, Biome.PLAINS, 0)
        setBiomeBase(Biome.JUNGLE_HILLS, Biome.TAIGA, 0)
        setBiomeBase(Biome.JUNGLE_EDGE, Biome.DESERT, 0)
        setBiomeBase(Biome.JUNGLE_MOUNTAINS, Biome.DESERT, 0)
        setBiomeBase(Biome.JUNGLE_EDGE_MOUNTAINS, Biome.DESERT, 0)
        setBiomeBase(Biome.DEEP_OCEAN, Biome.PLAINS, 0)
        setBiomeBase(Biome.SAVANNA_PLATEAU, Biome.PLAINS, 0)
        setBiomeBase(Biome.ROOFED_FOREST, Biome.DESERT, 0)
        setBiomeBase(Biome.STONE_BEACH, Biome.PLAINS, 0)
        setBiomeBase(Biome.JUNGLE, Biome.PLAINS, 128)
        setBiomeBase(Biome.JUNGLE_EDGE, Biome.DESERT, 128)
        setBiomeBase(Biome.SAVANNA, Biome.SAVANNA, 128)
        setBiomeBase(Biome.SAVANNA_PLATEAU, Biome.DESERT, 128)
        setBiomeBase(Biome.FOREST_HILLS, Biome.PLAINS, 0)
        setBiomeBase(Biome.BIRCH_FOREST_HILLS, Biome.PLAINS, 0)
        setBiomeBase(Biome.BIRCH_FOREST_HILLS, Biome.PLAINS, 128)
        setBiomeBase(Biome.BIRCH_FOREST_HILLS_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.BIRCH_FOREST_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.TAIGA, Biome.SAVANNA, 0)
        setBiomeBase(Biome.TAIGA, Biome.SAVANNA, 128)
        setBiomeBase(Biome.TAIGA_HILLS, Biome.SAVANNA, 0)
        setBiomeBase(Biome.TAIGA_MOUNTAINS, Biome.SAVANNA, 0)
        setBiomeBase(Biome.ICE_PLAINS, Biome.SAVANNA, 0)
        setBiomeBase(Biome.ICE_PLAINS, Biome.SAVANNA, 128)
        setBiomeBase(Biome.ICE_PLAINS_SPIKES, Biome.SAVANNA, 0)
        setBiomeBase(Biome.MEGA_SPRUCE_TAIGA, Biome.PLAINS, 0)
        setBiomeBase(Biome.MEGA_SPRUCE_TAIGA_HILLS, Biome.PLAINS, 0)
        setBiomeBase(Biome.MEGA_TAIGA, Biome.PLAINS, 0)
        setBiomeBase(Biome.MEGA_TAIGA, Biome.PLAINS, 128)
        setBiomeBase(Biome.MEGA_TAIGA_HILLS, Biome.PLAINS, 0)
        setBiomeBase(Biome.COLD_BEACH, Biome.DESERT, 0)
        setBiomeBase(Biome.COLD_TAIGA, Biome.PLAINS, 0)
        setBiomeBase(Biome.COLD_TAIGA, Biome.PLAINS, 128)
        setBiomeBase(Biome.COLD_TAIGA_HILLS, Biome.DESERT, 0)
        setBiomeBase(Biome.COLD_TAIGA_MOUNTAINS, Biome.DESERT, 0)
        setBiomeBase(Biome.FOREST, Biome.PLAINS, 0)
        setBiomeBase(Biome.ROOFED_FOREST_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA, Biome.PLAINS, 128)
        setBiomeBase(Biome.MESA_PLATEAU, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA_PLATEAU, Biome.PLAINS, 128)
        setBiomeBase(Biome.MESA_BRYCE, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA_PLATEAU_FOREST, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA_PLATEAU_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.MESA_PLATEAU_FOREST_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.EXTREME_HILLS, Biome.PLAINS, 0)
        setBiomeBase(Biome.EXTREME_HILLS, Biome.DESERT, 128)
        setBiomeBase(Biome.EXTREME_HILLS_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.EXTREME_HILLS_PLUS, Biome.DESERT, 0)
        setBiomeBase(Biome.EXTREME_HILLS_PLUS, Biome.DESERT, 128)
        setBiomeBase(Biome.EXTREME_HILLS_PLUS_MOUNTAINS, Biome.DESERT, 0)
        setBiomeBase(Biome.FROZEN_OCEAN, Biome.PLAINS, 0)
        setBiomeBase(Biome.FROZEN_RIVER, Biome.PLAINS, 0)
        setBiomeBase(Biome.ICE_MOUNTAINS, Biome.PLAINS, 0)
        setBiomeBase(Biome.SWAMPLAND, Biome.PLAINS, 0)
        setBiomeBase(Biome.SWAMPLAND_MOUNTAINS, Biome.PLAINS, 0)
    }

    private fun setBiomeBase(from: Biome, to: Biome, plus: Int)
    {
        BiomeBase.getBiomes()[CraftBlock.biomeToBiomeBase(from).id + plus] = CraftBlock.biomeToBiomeBase(to)
    }
}
