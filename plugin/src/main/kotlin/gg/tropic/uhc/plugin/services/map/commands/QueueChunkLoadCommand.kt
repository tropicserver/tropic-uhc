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
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.styles.prefix
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.io.File

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object QueueChunkLoadCommand : ScalaCommand()
{
    @CommandAlias("queue-chunk-reload")
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

        MapGenerationService
            .startWorldRegeneration(
                chunksPerRun ?: 100
            )
    }
}
