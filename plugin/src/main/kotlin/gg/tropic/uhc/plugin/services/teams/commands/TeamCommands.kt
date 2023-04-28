package gg.tropic.uhc.plugin.services.teams.commands

import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.CommandIssuer
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.annotation.PreCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.uhc.plugin.services.teams.gameType
import gg.tropic.uhc.plugin.services.teams.joinTeam
import gg.tropic.uhc.plugin.services.teams.sendTeamInviteTo
import gg.tropic.uhc.plugin.services.teams.team
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 4/26/2023
 */
@AutoRegister
@CommandAlias("team|teams")
@Conditions("team-game-required")
object TeamCommands : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("invite")
    @Description("Invite another user to join your team.")
    fun onInvite(player: ScalaPlayer, target: LemonPlayer)
    {
        val team = player.bukkit().team
            ?: throw ConditionFailedException(
                "You are not in a team right now!"
            )

        team.sendTeamInviteTo(target.uniqueId)
    }

    @Subcommand("join")
    @Description("Join a team via an invite.")
    fun onJoin(player: ScalaPlayer, team: Int)
    {
        player.bukkit().joinTeam(team)
    }

    @Subcommand("view")
    @Description("View details for a particular team.")
    fun onView(player: ScalaPlayer, @Optional team: Int?)
    {
        val teamId = team ?: player.bukkit().team?.id
        val team = CgsGameTeamService.teams[teamId]
            ?: throw ConditionFailedException(
                "Team #$teamId does not exist in this game."
            )

        player.sendMessage(
            "${CC.GREEN}Team #$teamId:",
            "${CC.GRAY}Participants:"
        )

        if (team.participants.isEmpty())
        {
            player.sendMessage("${CC.RED}No participants!")
        } else
        {
            team.participants.forEach {
                player.sendMessage("${CC.WHITE}- ${it.username()}")
            }
        }
    }
}
