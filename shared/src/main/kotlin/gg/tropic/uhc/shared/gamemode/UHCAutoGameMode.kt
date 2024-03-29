package gg.tropic.uhc.shared.gamemode

import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.lemon.LemonConstants
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.nio.file.Path

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object UHCAutoGameMode : CgsGameMode
{
    override fun getId() = "solo"
    override fun getName() = "Solo"

    override fun getMaterial() = Pair(Material.GOLDEN_APPLE, 0)

    override fun getDescription() = "${CC.GRAY}A solo game of UHC!"

    object UHCRandomArena : CgsGameArena
    {
        override fun getId() = "uhc"
        override fun getName() = "Random"

        override fun getMaterial() = Pair(Material.ENDER_PEARL, 0)
        override fun getDescription() = "UHC games have randomly generated arenas!"

        override fun getDirectory(): Path? = null

        override fun getBukkitWorldName() = "uhc_world"

        override fun getPreLobbyLocation() = if (LemonConstants.SERVER_NAME == "Glade")
        {
            Location(
                Bukkit.getWorld("lobby"),
                0.5, 65.0, 1.5, -0.0f, 0.0f
            )
        } else
        {
            Location(
                Bukkit.getWorld("lobby"),
                -44.5, 67.0, -0.5, -90.0f, 0.0f
            )
        }

        override fun getSpectatorLocation() = Location(
            Bukkit.getWorld("uhc_world"),
            0.5,
            Bukkit.getWorld("uhc_world")
                .getHighestBlockYAt(0, 0) + 15.0,
            0.5
        )
    }

    override fun getArenas() = listOf(UHCRandomArena)

    override fun getTeamSize() = 1 // team size
    override fun getMaxTeams() = 80
}
