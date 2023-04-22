package gg.tropic.uhc.plugin.services.border

import gg.scala.flavor.service.Service
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.scatter.remainingPlayers
import org.bukkit.ChatColor
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
        handlePlayers(border = size.toInt())
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

    fun handlePlayers(border: Int)
    {
        val world = mapWorld()

        for (player in remainingPlayers)
        {
            if (player.world.name == "uhc_world")
            {
                if (player.location.blockX > border)
                {
                    handleEffects(player)
                    player.teleport(
                        Location(
                            world,
                            (border - 2).toDouble(), player.location.blockY.toDouble(),
                            player.location.blockZ.toDouble()
                        )
                    )
                    if (player.location.blockY < world.getHighestBlockYAt(
                            player.location.blockX,
                            player.location.blockZ
                        )
                    )
                    {
                        player.teleport(
                            Location(
                                world,
                                player.location.blockX.toDouble(),
                                (world.getHighestBlockYAt(
                                    player.location.blockX,
                                    player.location.blockZ
                                ) + 2).toDouble(),
                                player.location.blockZ.toDouble()
                            )
                        )
                    }
                }
                if (player.location.blockZ > border)
                {
                    handleEffects(player)
                    player.teleport(
                        Location(
                            world,
                            player.location.blockX.toDouble(),
                            player.location.blockY.toDouble(),
                            (border - 2).toDouble()
                        )
                    )
                    if (player.location.blockY < world.getHighestBlockYAt(
                            player.location.blockX,
                            player.location.blockZ
                        )
                    )
                    {
                        player.teleport(
                            Location(
                                world,
                                player.location.blockX.toDouble(),
                                (world.getHighestBlockYAt(
                                    player.location.blockX,
                                    player.location.blockZ
                                ) + 2).toDouble(),
                                player.location.blockZ.toDouble()
                            )
                        )
                    }
                }
                if (player.location.blockX < -border)
                {
                    handleEffects(player)
                    player.teleport(
                        Location(
                            world,
                            (-border + 2).toDouble(), player.location.blockY.toDouble(),
                            player.location.blockZ.toDouble()
                        )
                    )
                    if (player.location.blockY < world.getHighestBlockYAt(
                            player.location.blockX,
                            player.location.blockZ
                        )
                    )
                    {
                        player.teleport(
                            Location(
                                world,
                                player.location.blockX.toDouble(),
                                (world.getHighestBlockYAt(
                                    player.location.blockX,
                                    player.location.blockZ
                                ) + 2).toDouble(),
                                player.location.blockZ.toDouble()
                            )
                        )
                    }
                }
                if (player.location.blockZ < -border)
                {
                    handleEffects(player)
                    player.teleport(
                        Location(
                            world,
                            player.location.blockX.toDouble(),
                            player.location.blockY.toDouble(),
                            (-border + 2).toDouble()
                        )
                    )
                    if (player.location.blockY < world.getHighestBlockYAt(
                            player.location.blockX,
                            player.location.blockZ
                        )
                    )
                    {
                        player.teleport(
                            Location(
                                world,
                                player.location.blockX.toDouble(),
                                (world.getHighestBlockYAt(
                                    player.location.blockX,
                                    player.location.blockZ
                                ) + 2).toDouble(),
                                player.location.blockZ.toDouble()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun handleEffects(player: Player)
    {
        player.world.playEffect(player.location, Effect.LARGE_SMOKE, 2, 2)
        player.playSound(player.location, Sound.EXPLODE, 1.0f, 2.0f)
        player.sendMessage(ChatColor.RED.toString() + "You've been teleported to a valid location inside the world border.")
    }
}
