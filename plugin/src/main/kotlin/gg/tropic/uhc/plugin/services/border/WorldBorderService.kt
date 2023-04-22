package gg.tropic.uhc.plugin.services.border

import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.mapWorld
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object WorldBorderService
{
    private var center: Location? = null

    val initialSize: Double
        get() = initialBorderSize.value.toDouble()

    var currentSize = initialBorderSize
        .value.toDouble()

    fun pushSizeUpdate(size: Double)
    {
        currentSize = size
        ensurePlayersWithinBorderBounds(border = size.toInt())
        synchronizeBukkitWorldBorder(size)
    }

    fun setCenter(center: Location) =
        apply {
            this.center = center
        }

    private fun synchronizeBukkitWorldBorder(size: Double)
    {
        val worldBorder = center!!.world.worldBorder
        worldBorder.setCenter(center!!.x, center!!.z)
        worldBorder.size = size
    }

    fun ensurePlayersWithinBorderBounds(border: Int)
    {
        Players.all()
            .filter {
                it.world.name == mapWorld().name
            }
            .forEach {
                val maximum = border / 2
                val minimum = -(maximum)

                val location = it.location.clone()
                var locationModified = false

                if (location.x < minimum)
                {
                    location.x = minimum + 2.5
                    locationModified = true
                }

                if (location.x > maximum)
                {
                    location.x = maximum - 2.5
                    locationModified = true
                }

                if (location.z < minimum)
                {
                    location.z = minimum + 2.5
                    locationModified = true
                }

                if (location.z > maximum)
                {
                    location.z = maximum - 2.5
                    locationModified = true
                }

                if (locationModified)
                {
                    it.teleport(location)
                    playBorderBoundTeleportationEffects(it)
                }
            }
    }

    private fun playBorderBoundTeleportationEffects(player: Player)
    {
        player.world.playEffect(player.location, Effect.LARGE_SMOKE, 2, 2)
        player.playSound(player.location, Sound.EXPLODE, 1.0f, 2.0f)
        player.sendMessage("${CC.RED}You've been teleported to a valid location inside the world border.")
    }
}
