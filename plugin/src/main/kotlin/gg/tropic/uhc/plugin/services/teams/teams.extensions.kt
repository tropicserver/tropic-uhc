package gg.tropic.uhc.plugin.services.teams

/**
 * @author GrowlyX
 * @since 4/25/2023
 */
const val allowGameTypeEditing = false
var gameType = GameTeamType.FFA

fun GameTeamType.compatibleWith(maxCount: Int) =
    maxCount % teamSize == 0
