package gg.tropic.uhc.shared.gamemode

import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.mode.CgsGameMode
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.nio.file.Path

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object UHCSoloGameMode : CgsGameMode
{
    override fun getId() = "solo_hosted"
    override fun getName() = "Hosted Solos"

    override fun getMaterial() = Pair(Material.GOLDEN_APPLE, 0)

    override fun getDescription() = "${CC.GRAY}A solo game of Hosted UHC!"

    override fun getArenas() = listOf(UHCAutoGameMode.UHCRandomArena)

    override fun getTeamSize() = 1 // team size
    override fun getMaxTeams() = 200
}
