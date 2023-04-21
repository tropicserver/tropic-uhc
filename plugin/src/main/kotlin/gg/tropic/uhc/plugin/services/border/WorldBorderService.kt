package gg.tropic.uhc.plugin.services.border

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import org.bukkit.Location

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
object WorldBorderService
{
    private var center: Location? = null
    private var size: Double = 5000.0

    @Configure
    fun configure()
    {

    }

    private fun synchronizeBukkitWorldBorder()
    {
        val worldBorder = center!!.world.worldBorder
        worldBorder.setCenter(center!!.x, center!!.z)

        worldBorder.damageAmount = 0.5
        worldBorder.damageBuffer = 3.5
        worldBorder.size = size
    }
}
