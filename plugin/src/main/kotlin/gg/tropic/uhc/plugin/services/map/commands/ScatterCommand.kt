package gg.tropic.uhc.plugin.services.map.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.map.MapGenerationService

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object ScatterCommand : ScalaCommand()
{
    @CommandAlias("scatter-test")
    @CommandPermission("uhc.command.scatter-test")
    fun onScatter(player: ScalaPlayer)
    {
        player.teleport(MapGenerationService.generateScatterLocation())
    }
}
