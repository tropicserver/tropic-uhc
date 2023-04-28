package gg.tropic.uhc.plugin.services.teams.commands

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.CommandIssuer
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.PreCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.tropic.uhc.plugin.services.teams.gameType

/**
 * @author GrowlyX
 * @since 4/26/2023
 */
@AutoRegister
@CommandAlias("team|teams")
object TeamCommands : ScalaCommand()
{
    @Default
    @HelpCommand
    @Conditions("team-game-required")
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }
}
