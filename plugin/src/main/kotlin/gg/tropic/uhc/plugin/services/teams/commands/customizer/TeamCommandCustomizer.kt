package gg.tropic.uhc.plugin.services.teams.commands.customizer

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.tropic.uhc.plugin.services.teams.gameType

/**
 * @author GrowlyX
 * @since 4/27/2023
 */
object TeamCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandConditions
            .addCondition("team-game-required") {
                if (gameType.teamSize <= 1)
                    throw ConditionFailedException(
                        "You cannot use this command in an FFA game!"
                    )
            }
    }
}
