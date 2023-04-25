package gg.tropic.uhc.plugin.engine

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.runnable.state.EndedStateRunnable
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.lemon.util.task.DiminutionRunnable
import gg.tropic.uhc.plugin.services.border.BorderUpdateEventExecutor
import gg.tropic.uhc.plugin.services.border.WorldBorderService
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.scatter.ScatterService
import gg.tropic.uhc.plugin.services.scatter.remainingPlayers
import gg.tropic.uhc.plugin.services.teams.gameType
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
object UHCScoreboardRenderer : CgsGameScoreboardRenderer
{
    private val footerPadding = "${CC.GRAY} ".repeat(10)

    override fun getTitle() = "${CC.B_PRI}UHC"

    override fun render(
        lines: LinkedList<String>, player: Player, state: CgsGameState
    )
    {
        lines += ""

        when (state)
        {
            CgsGameState.WAITING ->
            {
                if (MapGenerationService.generating)
                {
                    lines += "${CC.PRI}Generating:"
                    lines += "Progress: ${CC.PRI}${
                        MapGenerationService.generation?.completionStatus() ?: "0.0%"
                    }"
                } else
                {
                    lines += "${CC.GRAY}Game being prepared"
                    lines += "${CC.GRAY}while waiting for more"
                    lines += "${CC.GRAY}players$ellipsis"
                }

                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
                lines += "${CC.WHITE}Mode: ${CC.PRI}${gameType.name}"
                lines += "${CC.WHITE}Players: ${CC.PRI}${
                    Bukkit.getOnlinePlayers().size
                }/${
                    Bukkit.getMaxPlayers()
                }"
            }

            CgsGameState.STARTING ->
            {
                lines += "${CC.GOLD}Scattering:"
                lines += "${CC.WHITE}Scattered: ${CC.GOLD}${ScatterService.playersScattered.size}/${ScatterService.gameFillCount}"
                lines += "${CC.WHITE}Starts in: ${CC.GOLD}${
                    TimeUtil.formatIntoMMSS(StartingStateRunnable.PRE_START_TIME)
                }"
                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
                lines += "${CC.WHITE}Mode: ${CC.PRI}${gameType.name}"
            }

            CgsGameState.STARTED ->
            {
                lines += "Game time: ${CC.PRI}${
                    TimeUtil.formatIntoMMSS(((System.currentTimeMillis() - CgsGameEngine.INSTANCE.gameStart) / 1000).toInt())
                }"
                lines += "Remaining: ${CC.PRI}${
                    remainingPlayers.size
                }/${
                    ScatterService.gameFillCount
                }"
                lines += "Kills: ${CC.PRI}${
                    CgsGameEngine.INSTANCE
                        .getStatistics(
                            CgsPlayerHandler.find(player)!!
                        )
                        .gameKills.value
                }"
                lines += "Border: ${CC.PRI}${
                    Numbers.format(WorldBorderService.currentSize.toInt())
                }${
                    if (BorderUpdateEventExecutor.currentBorderUpdater != null)
                        " ${CC.GRAY}(${TimeUtil.formatIntoAbbreviatedString(BorderUpdateEventExecutor.currentBorderUpdater!!.seconds)})" else ""
                }"
            }

            CgsGameState.ENDED ->
            {
                lines += "${CC.GREEN}Congrats to"
                lines += "${CC.B_GREEN}${
                    CgsGameEngine.INSTANCE.winningTeam
                        .participants.first().username()
                }"
                lines += "${CC.GREEN}for winning the game!"
                lines += ""
                lines += "${CC.WHITE}Host: ${CC.RED}${hostDisplayName()}"
                lines += "${CC.WHITE}Participants: ${CC.GOLD}${ScatterService.gameFillCount}"
                lines += "${CC.WHITE}Ends in: ${CC.GOLD}${
                    60 - EndedStateRunnable.currentTick
                }s"
            }

            else ->
            {
            }
        }

        lines += ""
        lines += "${CC.GRAY}${LemonConstants.WEB_LINK} $footerPadding"
    }
}

fun createRunner(
    seconds: Int,
    end: () -> Unit,
    update: (Int) -> Unit
) = object : CountdownRunnable(seconds + 1)
{
    override fun getSeconds() = listOf(
        18000, 14400, 10800, 7200, 3600, 2700, 1800,
        900, 600, 300, 240, 180, 120, 60, 50, 40, 30,
        15, 10, 5, 4, 3, 2, 1
    )

    override fun onEnd() = end()
    override fun onRun() = update(this.seconds)
}.apply {
    Schedulers
        .sync()
        .runRepeating(
            this, 0L, 20L
        )
        .let {
            this.task = it
        }
}

abstract class CountdownRunnable(var seconds: Int) : Runnable
{
    var task: Task? = null

    override fun run()
    {
        seconds--

        if (getSeconds().contains(seconds))
        {
            onRun()
        } else if (seconds == 0)
        {
            onEnd()
            task?.closeAndReportException()
        }
    }

    fun broadcast(message: String)
    {
        Bukkit.broadcastMessage(net.evilblock.cubed.util.Color.translate(message))
    }

    abstract fun onRun()
    abstract fun onEnd()
    abstract fun getSeconds(): List<Int>
}

