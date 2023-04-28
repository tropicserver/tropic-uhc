package gg.tropic.uhc.plugin.services.teams

import gg.scala.cgs.common.teams.CgsGameTeam
import gg.scala.cgs.common.teams.CgsGameTeamService
import org.bukkit.entity.Player

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
