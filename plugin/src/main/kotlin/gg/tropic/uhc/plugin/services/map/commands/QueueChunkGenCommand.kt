package gg.tropic.uhc.plugin.services.map.commands

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.map.mapNetherWorld
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.styles.prefix
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object QueueChunkGenCommand : ScalaCommand()
{
    @CommandAlias("generate-nether")
    @CommandPermission("uhc.command.regen")
    fun onGenerateNether(player: ScalaPlayer)
    {
        if (MapGenerationService.generating)
        {
            throw ConditionFailedException("The map is already generating!")
        }

        if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
        {
            throw ConditionFailedException("You cannot regen at this time!")
        }

        WorldBorderService
            .pushSizeUpdate(
                initialBorderSize.value.toDouble()
            )

        MapGenerationService
            .startWorldRegeneration(
                100, mapNetherWorld()
            )
    }

    @CommandAlias("generate-overworld")
    @CommandPermission("uhc.command.regen")
    fun onQueueChunkLoad(
        player: ScalaPlayer, @Optional chunksPerRun: Int?
    )
    {
        if (MapGenerationService.generating)
        {
            throw ConditionFailedException("The map is already generating!")
        }

        if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
        {
            throw ConditionFailedException("You cannot regen at this time!")
        }

        player.sendMessage(
            "$prefix${CC.GREEN}Starting a chunk reload task with a chunk load freq of ${chunksPerRun ?: 100}."
        )

        WorldBorderService
            .pushSizeUpdate(
                initialBorderSize.value.toDouble()
            )

        MapGenerationService
            .startWorldRegeneration(
                chunksPerRun ?: 100, mapWorld()
            )
    }
}
