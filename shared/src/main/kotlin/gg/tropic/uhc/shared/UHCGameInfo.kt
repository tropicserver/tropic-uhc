package gg.tropic.uhc.shared

import gg.scala.cgs.common.information.CgsGameAwardInfo
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.tropic.uhc.shared.gamemode.UHCSoloGameMode

/**
 * @author GrowlyX
 * @since 4/20/2023
 */
object UHCGameInfo : CgsGameGeneralInfo(
    fancyNameRender = "UHC",
    gameVersion = 1.0F,
    minimumPlayers = 2,
    startingCountdownSec = 61,
    awards = CgsGameAwardInfo(
        awardCoins = true,
        winningCoinRange = 1000..2000,
        participationCoinRange = 250..300
    ),
    preStartVoting = false,
    disqualifyOnLogout = false,
    spectateOnDeath = true,
    showTabHearts = true,
    showNameHearts = true,
    usesCustomArenaWorld = true,
    gameModes = listOf(UHCSoloGameMode)
)
{
    init
    {
        this.requiresNoManualConfiguration = false
        this.timeUntilShutdown = 60
    }
}
