package gg.tropic.uhc.plugin.services.map

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.biome.BiomeSwapper
import gg.tropic.uhc.plugin.services.map.threshold.BiomeThreshold
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Events
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntitySpawnEvent
import java.io.File
import java.io.IOException

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object MapGenerationService
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    private lateinit var cuboid: Cuboid

    var generating = false
    var generation: MapChunkLoadTask? = null

    @Configure
    fun configure()
    {
        Events
            .subscribe(CgsGameEngine.CgsGameForceStartEvent::class.java)
            .handler {
                if (generating)
                {
                    it.isCancelled = true
                    it.starter.sendMessage("${CC.RED}The world is currently being generated! You're unable to start the game at this time.")
                }
            }
            .bindWith(plugin)

        val lockFile = File(Bukkit.getWorldContainer(), "tropic.uhc.lock")

        if (!lockFile.exists())
        {
            deleteExistingWorld()
            createNewWorld()

            lockFile.createNewFile()
        } else
        {
            BiomeSwapper.swapBiomes()
            val text = lockFile.readText()

            if (text != "")
            {
                initialBorderSize.valueInternal = text.toInt()
                plugin.logger.info("Loaded cached border size of $text")
            }

            val uhcWorld = Bukkit.createWorld(
                WorldCreator("uhc_world")
                    .environment(World.Environment.NORMAL)
                    .type(WorldType.NORMAL)
            )

            uhcWorld.setGameRuleValue("doDaylightCycle", "false")
            uhcWorld.setGameRuleValue("doFireTick", "false")
            uhcWorld.setGameRuleValue("naturalRegeneration", "false")

            uhcWorld.time = 0
            uhcWorld.pvp = false
            uhcWorld.difficulty = Difficulty.NORMAL

            cuboid = Cuboid(
                mapWorld(),
                -(WorldBorderService.initialSize.toInt() / 2), 100, -(WorldBorderService.initialSize.toInt() / 2),
                WorldBorderService.initialSize.toInt() / 2, 100, WorldBorderService.initialSize.toInt() / 2
            )

            uhcWorld.setSpawnLocation(
                cuboid.center.x.toInt(),
                cuboid.center.y.toInt(),
                cuboid.center.z.toInt()
            )

            // internal world/arena configuration for CGS services
            CgsGameArenaHandler.world = uhcWorld
            CgsGameArenaHandler.arena = CgsGameEngine
                .INSTANCE.gameMode.getArenas().random()

            CgsGameEngine.INSTANCE.gameArena =
                CgsGameArenaHandler.arena

            plugin.logger.info("Loaded world from lock file.")
        }

        val uhcNether = Bukkit.createWorld(
            WorldCreator("uhc_nether")
                .environment(World.Environment.NETHER)
                .type(WorldType.NORMAL)
        )

        uhcNether.setGameRuleValue("doDaylightCycle", "false")
        uhcNether.setGameRuleValue("naturalRegeneration", "false")

        uhcNether.time = 0
        uhcNether.pvp = false

        WorldBorderService
            .setCenter(cuboid.center)
            .pushSizeUpdate(
                WorldBorderService.initialSize
            )

        startWorldRegeneration()
    }

    fun startWorldRegeneration(chunksPerRun: Int = 100)
    {
        if (generating)
        {
            return
        }

        val loadTask = MapChunkLoadTask(
            Bukkit.getServer(),
            mapWorld().name,
            CoordXZ.chunkToBlock(13),
            chunksPerRun,
            3
        )

        generating = true
        generation = loadTask

        Bukkit.setWhitelist(true)
        Bukkit.broadcastMessage(
            "$prefix${CC.GRAY}Starting to load chunks with ${WorldBorderService.initialSize} as the border size. Whitelist is now enabled as a precaution."
        )

        loadTask.finish = Runnable {
            generation = null
            generating = false

            Players.all()
                .filter { it.isOp }
                .forEach {
                    it.sendMessage("$prefix${CC.GREEN}Completed chunk generation. The game is now ready to begin.")
                }
        }

        loadTask.cuboid = cuboid

        loadTask.setTaskID(
            Bukkit.getServer().scheduler
                .scheduleSyncRepeatingTask(
                    plugin, loadTask, 5, 5
                )
        )
    }

    fun generateScatterLocation(): Location
    {
        val halfRange = WorldBorderService.initialSize.toInt() / 2
        val randomX = (-halfRange..halfRange).random()
        val randomZ = (-halfRange..halfRange).random()

        return Location(
            mapWorld(),
            randomX.toDouble(),
            mapWorld()
                .getHighestBlockYAt(randomX, randomZ)
                .toDouble(),
            randomZ.toDouble()
        )
    }

    private fun createNewWorld()
    {
        val uhcWorld = Bukkit.createWorld(
            WorldCreator("uhc_world")
                .environment(World.Environment.NORMAL)
                .type(WorldType.NORMAL)
        )

        uhcWorld.setGameRuleValue("doDaylightCycle", "false")
        uhcWorld.setGameRuleValue("doFireTick", "false")
        uhcWorld.setGameRuleValue("naturalRegeneration", "false")

        uhcWorld.time = 0
        uhcWorld.pvp = false
        uhcWorld.difficulty = Difficulty.NORMAL

        cuboid = Cuboid(
            mapWorld(),
            -(WorldBorderService.initialSize.toInt() / 2), 100, -(WorldBorderService.initialSize.toInt() / 2),
            WorldBorderService.initialSize.toInt() / 2, 100, WorldBorderService.initialSize.toInt() / 2
        )

        uhcWorld.setSpawnLocation(
            cuboid.center.x.toInt(),
            cuboid.center.y.toInt(),
            cuboid.center.z.toInt()
        )

        var waterCount = 0
        var limit = 0

        Bukkit.getLogger().info("Loaded a new world ${cuboid.center.x}, ${cuboid.center.y}, ${cuboid.center.z}.")

        var flag = false

        for (i in -100..100)
        {
            var isInvalid = false

            for (j in -100..100)
            {
                val biome = uhcWorld.getBiome(i, j)
                val threshold = isValidBiome(biome, i, j)

                if (threshold == BiomeThreshold.DISALLOWED)
                {
                    Bukkit.getLogger().info("Biome at " + i + " " + j + " is " + biome.name)
                    Bukkit.getLogger().info("Invalid biome!")
                    isInvalid = true
                    break
                } else if (threshold == BiomeThreshold.LIMITED)
                {
                    if (++limit >= 13000)
                    {
                        Bukkit.getLogger().info("Too much hills/forests/etc")
                        Bukkit.getLogger().info("Invalid biome!")
                        isInvalid = true
                        break
                    }
                }

                val isCenter = i >= -50 && i <= 50 && j >= -50 && j <= 50

                if (isCenter)
                {
                    val block = uhcWorld.getHighestBlockAt(i, j).location.add(0.0, -1.0, 0.0).block
                    if (block.type == Material.STATIONARY_WATER || block.type == Material.WATER || block.type == Material.LAVA || block.type == Material.STATIONARY_LAVA)
                    {
                        ++waterCount
                    }
                }

                if (waterCount >= 1000)
                {
                    Bukkit.getLogger().info("Invalid center, too much water/lava. ($waterCount)")
                    isInvalid = true
                    break
                }
            }

            if (isInvalid)
            {
                flag = true
                break
            }
        }

        if (flag)
        {
            Bukkit.getServer().unloadWorld(uhcWorld, false)
            File(Bukkit.getWorldContainer().toString() + File.separator + "uhc_world").deleteRecursively()
            createNewWorld()
            return
        } else
        {
            Bukkit.getLogger().info("Found a good seed (" + uhcWorld.seed + ").")
        }

        BiomeSwapper.swapBiomes()

        // Create Lock
        val lock = File("uhc_world", "gen.lock")
        try
        {
            lock.createNewFile()
        } catch (e: IOException)
        {
            e.printStackTrace()
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop")
            return
        }

        WorldBorderService
            .setCenter(cuboid.center)

        // internal world/arena configuration for CGS services
        CgsGameArenaHandler.world = uhcWorld
        CgsGameArenaHandler.arena = CgsGameEngine
            .INSTANCE.gameMode.getArenas().random()

        CgsGameEngine.INSTANCE.gameArena =
            CgsGameArenaHandler.arena
    }

    private fun deleteExistingWorld()
    {
        val uhcWorld = Bukkit.getWorld("uhc_world")

        if (uhcWorld != null)
        {
            Bukkit.unloadWorld(uhcWorld, false)
            uhcWorld.worldFolder.deleteRecursively()
        }

        val worldNether = Bukkit.getWorld("uhc_nether")

        if (worldNether != null)
        {
            Bukkit.unloadWorld(worldNether, false)
            worldNether.worldFolder.deleteRecursively()
        }
    }

    private fun isValidBiome(biome: Biome, i: Int, j: Int): BiomeThreshold
    {
        val flag = i <= 100 && i >= -100 && j <= 100 && j >= -100
        if (biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.DESERT_MOUNTAINS || biome == Biome.PLAINS || biome == Biome.SUNFLOWER_PLAINS || biome == Biome.SWAMPLAND || biome == Biome.SWAMPLAND_MOUNTAINS || biome == Biome.SMALL_MOUNTAINS || biome == Biome.SAVANNA || biome == Biome.SAVANNA_MOUNTAINS || biome == Biome.SAVANNA_PLATEAU || biome == Biome.SAVANNA_PLATEAU_MOUNTAINS || biome == Biome.RIVER || biome == Biome.FROZEN_RIVER || biome == Biome.ICE_PLAINS)
        {
            return BiomeThreshold.ALLOWED
        } else if (flag && (biome == Biome.FOREST || biome == Biome.FOREST_HILLS || biome == Biome.BIRCH_FOREST || biome == Biome.BIRCH_FOREST_HILLS || biome == Biome.BIRCH_FOREST_HILLS_MOUNTAINS || biome == Biome.BIRCH_FOREST_MOUNTAINS || biome == Biome.TAIGA || biome == Biome.TAIGA_HILLS || biome == Biome.TAIGA_MOUNTAINS || biome == Biome.ICE_PLAINS_SPIKES || biome == Biome.MEGA_SPRUCE_TAIGA || biome == Biome.MEGA_SPRUCE_TAIGA_HILLS || biome == Biome.MEGA_TAIGA || biome == Biome.MEGA_TAIGA_HILLS || biome == Biome.FLOWER_FOREST || biome == Biome.COLD_BEACH || biome == Biome.COLD_TAIGA || biome == Biome.COLD_TAIGA_HILLS || biome == Biome.COLD_TAIGA_HILLS || biome == Biome.COLD_TAIGA_MOUNTAINS))
        {
            return BiomeThreshold.LIMITED
        } else if (flag && (biome == Biome.ROOFED_FOREST || biome == Biome.ROOFED_FOREST_MOUNTAINS || biome == Biome.MESA || biome == Biome.MESA_PLATEAU || biome == Biome.MESA_BRYCE || biome == Biome.MESA_PLATEAU_FOREST || biome == Biome.MESA_PLATEAU_FOREST_MOUNTAINS || biome == Biome.MESA_PLATEAU_MOUNTAINS || biome == Biome.EXTREME_HILLS || biome == Biome.EXTREME_HILLS_MOUNTAINS || biome == Biome.EXTREME_HILLS_PLUS || biome == Biome.EXTREME_HILLS_PLUS_MOUNTAINS || biome == Biome.FROZEN_OCEAN || biome == Biome.ICE_MOUNTAINS))
        {
            return BiomeThreshold.DISALLOWED
        }
        return if (flag) BiomeThreshold.DISALLOWED else BiomeThreshold.ALLOWED
    }
}
