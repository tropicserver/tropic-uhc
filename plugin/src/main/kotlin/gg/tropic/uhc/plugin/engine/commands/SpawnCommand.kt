package gg.tropic.uhc.plugin.engine.commands

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.shared.gamemode.UHCSoloGameMode

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object SpawnCommand : ScalaCommand()
{
    @CommandAlias("spawn")
    fun onSpawn(player: ScalaPlayer)
    {
        if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
        {
            throw ConditionFailedException("You cannot go back to spawn right now!")
        }

        player.teleport(
            UHCSoloGameMode.getArenas()
                .first().getPreLobbyLocation()
        )
    }
}
