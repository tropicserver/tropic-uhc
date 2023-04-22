package gg.tropic.uhc.plugin.services.map.biome;

import net.minecraft.server.v1_8_R3.BiomeBase;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.Arrays;

public class BiomeSwapper {

    public static void swapBiomes() {
        swapBiome(Biome.OCEAN, Biome.FOREST);
        swapBiome(Biome.DEEP_OCEAN, Biome.FOREST);
        swapBiome(Biome.EXTREME_HILLS, Biome.PLAINS);
        swapBiome(Biome.EXTREME_HILLS_PLUS, Biome.PLAINS);
        swapBiome(Biome.DESERT_HILLS, Biome.PLAINS);
        swapBiome(Biome.FROZEN_OCEAN, Biome.FOREST);
        swapBiome(Biome.ICE_PLAINS, Biome.PLAINS);
        swapBiome(Biome.TAIGA_HILLS, Biome.FOREST);
        swapBiome(Biome.SMALL_MOUNTAINS, Biome.PLAINS);
        swapBiome(Biome.JUNGLE, Biome.PLAINS);
        swapBiome(Biome.JUNGLE_HILLS, Biome.PLAINS);
        swapBiome(Biome.JUNGLE_EDGE, Biome.PLAINS);
        swapBiome(Biome.COLD_TAIGA, Biome.PLAINS);
        swapBiome(Biome.COLD_TAIGA_HILLS, Biome.PLAINS);
        swapBiome(Biome.ROOFED_FOREST, Biome.FOREST);
        swapBiome(Biome.ROOFED_FOREST_M, Biome.FOREST);
        swapBiome(Biome.MEGA_TAIGA_HILLS, Biome.PLAINS);
        swapBiome(Biome.MEGA_TAIGA, Biome.PLAINS);
        swapBiome(Biome.SAVANNA, Biome.FOREST);
        swapBiome(Biome.SAVANNA_PLATEAU, Biome.FOREST);
        swapBiome(Biome.MESA, Biome.PLAINS);
        swapBiome(Biome.MESA_PLATEAU, Biome.PLAINS);
        swapBiome(Biome.MESA_PLATEAU_F, Biome.PLAINS);
        swapBiome(Biome.ICE_PLAINS_SPIKES, Biome.PLAINS);
        swapBiome(Biome.EXTREME_HILLS_M, Biome.PLAINS);
        swapBiome(Biome.EXTREME_HILLS_M_PLUS, Biome.PLAINS);
        swapBiome(Biome.MEGA_SPRUCE_TAIGA, Biome.FOREST);
        swapBiome(Biome.JUNGLE_M, Biome.FOREST);
        swapBiome(Biome.JUNGLE_EDGE_M, Biome.PLAINS);
        swapBiome(Biome.MESA_BRYCE, Biome.PLAINS);
        swapBiome(Biome.SAVANNA_M, Biome.PLAINS);
        swapBiome(Biome.MESA_PLATEAU_F_M, Biome.PLAINS);
        swapBiome(Biome.PLATEAU_M, Biome.PLAINS);
        swapBiome(Biome.PLATEAUM, Biome.PLAINS);
        swapBiome(Biome.MUSHROOM_ISLAND, Biome.PLAINS);
        swapBiome(Biome.MUSHROOM_SHORE, Biome.PLAINS);
    }

    private static void swapBiome(BiomeSwapper.Biome oldBiome, BiomeSwapper.Biome newBiome) {
        if (oldBiome.getId() != BiomeSwapper.Biome.SKY.getId()) {
            BiomeBase[] biomes = getMcBiomes();
            biomes[oldBiome.getId()] = getOrigBiome(newBiome.getId());
        } else {
            Bukkit.getLogger().warning("Cannot swap SKY biome!");
        }
    }

    private static BiomeBase[] getMcBiomesCopy() {
        BiomeBase[] b = getMcBiomes();
        return Arrays.copyOf(b, b.length);
    }

    private static BiomeBase[] getMcBiomes() {
        try {
            Field biomeF = BiomeBase.class.getDeclaredField("biomes");
            biomeF.setAccessible(true);
            return (BiomeBase[]) biomeF.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return new BiomeBase[256];
    }

    private static BiomeBase getOrigBiome(int value) {
        return getMcBiomesCopy()[value];
    }

    public enum Biome {
        OCEAN(0), PLAINS(1), DESERT(2), EXTREME_HILLS(3), FOREST(4), TAIGA(5), SWAMPLAND(6),
        RIVER(7), HELL(8), SKY(9), FROZEN_OCEAN(10), FROZEN_RIVER(11), ICE_PLAINS(12),
        ICE_MOUNTAINS(13), MUSHROOM_ISLAND(14), MUSHROOM_SHORE(15), BEACH(16),
        DESERT_HILLS(17), FOREST_HILLS(18), TAIGA_HILLS(19), SMALL_MOUNTAINS(20),
        JUNGLE(21), JUNGLE_HILLS(22), JUNGLE_EDGE(23), DEEP_OCEAN(24),
        STONE_BEACH(25), COLD_BEACH(26), BIRCH_FOREST(27), BIRCH_FOREST_HILLS(28),
        ROOFED_FOREST(29), COLD_TAIGA(30), COLD_TAIGA_HILLS(31), MEGA_TAIGA(32),
        MEGA_TAIGA_HILLS(33), EXTREME_HILLS_PLUS(34), SAVANNA(35), SAVANNA_PLATEAU(36),
        MESA(37), MESA_PLATEAU_F(38), MESA_PLATEAU(39), ICE_PLAINS_SPIKES(140),
        EXTREME_HILLS_M(131), EXTREME_HILLS_M_PLUS(162), MEGA_SPRUCE_TAIGA(160),
        ROOFED_FOREST_M(157), SWAMPLAND_M(134), JUNGLE_M(149), JUNGLE_EDGE_M(151),
        MESA_BRYCE(165), SAVANNA_M(163), MESA_PLATEAU_F_M(166), PLATEAU_M(164), PLATEAUM(167);

        private int id;

        Biome(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }
}
