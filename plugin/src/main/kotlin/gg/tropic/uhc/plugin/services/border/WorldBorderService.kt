package gg.tropic.uhc.plugin.services.border

import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.services.border.bedrock.BedrockBorderMechanism
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.mapNetherWorld
import gg.tropic.uhc.plugin.services.map.mapWorld
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object WorldBorderService
{
    private var center: Pair<Double, Double>? = null

    val initialSize: Double
        get() = initialBorderSize.value.toDouble()

    var currentSize = initialBorderSize
        .value.toDouble()

    fun pushSizeUpdate(size: Double)
    {
        currentSize = size

        listOf(mapWorld(), mapNetherWorld())
            .forEach {
                ensurePlayersWithinBorderBounds(
                    border = size.toInt(), world = it.name
                )
            }

        synchronizeBukkitWorldBorder(size)

        if (size <= 50)
        {
            BedrockBorderMechanism
                .configureBedrockBorder(size)
        }
    }

    fun setCenter(center: Location) =
        apply {
            this.center = center.x to center.z
        }

    private fun synchronizeBukkitWorldBorder(size: Double)
    {
        listOf(mapWorld(), mapNetherWorld())
            .forEach {
                val worldBorder = it.worldBorder
                worldBorder.setCenter(center!!.first, center!!.second)
                worldBorder.size = size
            }
    }

    private const val BOUND_OFFSET = 5.5

    fun ensurePlayersWithinBorderBounds(border: Int, world: String)
    {
        Players.all()
            .filter {
                it.world.name == world
            }
            .forEach {
                val maximum = border / 2
                val minimum = -(maximum)

                val location = it.location.clone()
                var locationModified = false

                if (location.x < minimum)
                {
                    location.x = minimum + BOUND_OFFSET
                    locationModified = true
                }

                if (location.x > maximum)
                {
                    location.x = maximum - BOUND_OFFSET
                    locationModified = true
                }

                if (location.z < minimum)
                {
                    location.z = minimum + BOUND_OFFSET
                    locationModified = true
                }

                if (location.z > maximum)
                {
                    location.z = maximum - BOUND_OFFSET
                    locationModified = true
                }

                if (locationModified)
                {
                    location.y = location.world
                        .getHighestBlockYAt(location)
                        .toDouble()
                    location.y = location.y + 2.0

                    it.teleport(location)
                    playBorderBoundTeleportationEffects(it)
                }
            }
    }

    fun flatZone(size: Int)
    {
        for (x in -size until size)
        {
            for (y in 59..149)
            {
                for (z in -size until size)
                {
                    Location(
                        Bukkit.getWorld("uhc_world"),
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble()
                    ).block.type =
                        Material.AIR
                }
            }
        }

        for (x in -size until size)
        {
            for (y in 59..59)
            {
                for (z in -size until size)
                {
                    Location(
                        Bukkit.getWorld("uhc_world"),
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble()
                    ).block.type =
                        Material.BEDROCK
                }
            }
        }
        for (x in -size until size)
        {
            for (y in 60..60)
            {
                for (z in -size until size)
                {
                    Location(
                        Bukkit.getWorld("uhc_world"),
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble()
                    ).block.type =
                        Material.GRASS
                }
            }
        }

        Bukkit.getOnlinePlayers().forEach { player: Player? ->
            val location = player!!.location
            player.teleport(
                Location(
                    location.world,
                    location.x,
                    location.world.getHighestBlockYAt(location.blockX, location.blockZ).toDouble(),
                    location.z,
                    location.yaw,
                    location.pitch
                )
            )
        }
    }

    private fun playBorderBoundTeleportationEffects(player: Player)
    {
        player.world.playEffect(player.location, Effect.LARGE_SMOKE, 2, 2)
        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 0.5f)
        player.sendMessage("${CC.RED}You've been teleported to a valid location inside the world border.")
    }
}
