package gg.tropic.uhc.plugin.services.border

import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import org.bukkit.Location

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

        worldBorder.damageAmount = 0.5
        worldBorder.damageBuffer = 3.5
        worldBorder.size = size
    }
}
