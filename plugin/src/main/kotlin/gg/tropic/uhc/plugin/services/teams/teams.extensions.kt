package gg.tropic.uhc.plugin.services.teams

import gg.scala.cgs.common.teams.CgsGameTeam
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.uhc.plugin.services.styles.teamPrefix
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/25/2023
 */
const val allowGameTypeEditing = true
var gameType = GameTeamType.FFA

fun GameTeamType.compatibleWith(maxCount: Int) =
    maxCount % teamSize == 0

val Player.team: CgsGameTeam?
    get() = CgsGameTeamService
        .getTeamOf(this)

fun UUID.teamInviteFrom(teamId: Int) = GameTeamService.teamInvites
    .asMap().entries.firstOrNull {
        it.value.first == teamId && it.value.second == this
    }

fun Player.joinTeam(teamId: Int)
{
    val invite = uniqueId.teamInviteFrom(teamId)
        ?: throw ConditionFailedException(
            "You do not have a team invite from team #$teamId!"
        )

    val team = CgsGameTeamService.teams[teamId]
        ?: throw ConditionFailedException(
            "The team #$teamId does not exist this game."
        )

    if (team.participants.size >= gameType.teamSize)
    {
        throw ConditionFailedException(
            "${CC.RED}This team is already full!"
        )
    }

    GameTeamService.teamInvites.invalidate(invite.key)
    var shouldEliminateAgain = false

    this.team?.apply {
        participants.remove(uniqueId)
        shouldEliminateAgain = eliminated.remove(uniqueId)
    }

    team.apply {
        participants.add(uniqueId)
        if (shouldEliminateAgain)
        {
            eliminated.add(uniqueId)
        }
    }

    team.participants
        .mapNotNull {
            Bukkit.getPlayer(it)
        }
        .forEach {
            it.sendMessage("$teamPrefix${CC.GREEN}${uniqueId.username()}${CC.SEC} joined your team.")
        }
}

fun CgsGameTeam.sendTeamInviteTo(uniqueId: UUID)
{
    if (uniqueId.teamInviteFrom(id) != null)
    {
        throw ConditionFailedException(
            "Your team has already sent out a team invite to this player!"
        )
    }

    if (participants.size >= gameType.teamSize)
    {
        throw ConditionFailedException(
            "Your team can not fit any more players!"
        )
    }

    if (participants.contains(uniqueId))
    {
        throw ConditionFailedException(
            "This player is already in a team!"
        )
    }

    val player = Bukkit.getPlayer(uniqueId)
        ?: throw ConditionFailedException(
            "This player is no longer online!"
        )

    if (player.hasMetadata("teaminvites-disabled"))
    {
        throw ConditionFailedException(
            "This player has team invites disabled!"
        )
    }

    GameTeamService
        .teamInvites.put(
            UUID.randomUUID(),
            id to uniqueId
        )

    participants
        .mapNotNull {
            Bukkit.getPlayer(it)
        }
        .forEach {
            it.sendMessage("$teamPrefix${CC.GOLD}${uniqueId.username()}${CC.SEC} was invited to join your team. They have 30 seconds to accept.")
        }

    FancyMessage()
        .withMessage(
            "${CC.SEC}You were invited to join team ${CC.GOLD}#$id${CC.YELLOW}! "
        )
        .withMessage("${CC.GREEN}(Click to join)")
        .andHoverOf(
            "${CC.GREEN}Abandon your team to join team ${CC.PINK}#$id${CC.GREEN}!"
        )
        .andCommandOf(
            ClickEvent.Action.RUN_COMMAND,
            "/team join $id"
        )
        .sendToPlayer(player)
}
