package gg.tropic.uhc.plugin.services.border

import gg.tropic.uhc.plugin.engine.CountdownRunnable
import gg.tropic.uhc.plugin.services.configurate.borderDecreaseAmount
import gg.tropic.uhc.plugin.services.configurate.borderShrink
import gg.tropic.uhc.plugin.services.configurate.firstShrink
import gg.tropic.uhc.plugin.services.configurate.flatMeetup
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
object BorderUpdateEventExecutor
{
    private val broadcastIntervals = listOf(
        18000, 14400, 10800, 7200, 3600, 2700, 1800, 900, 600,
        300, 240, 180, 120, 60, 50, 40, 30, 15, 10, 5, 4, 3, 2, 1
    )

    var currentUpdateTask: Task? = null
    var currentBorderUpdater: BorderUpdateRunnable? = null

    private val oneHundredIntervals = arrayOf(100, 50, 25, 10)
    private var indexForHundreds = -1

    fun start()
    {
        val runnable = BorderUpdateRunnable(
            firstShrink.value * 60,
            getNextBorder()
        )

        WorldBorderService
            .pushSizeUpdate(
                WorldBorderService.currentSize
            )

        currentBorderUpdater = runnable
        currentUpdateTask = Schedulers
            .async()
            .runRepeating(
                runnable,
                0L, 20L
            )
    }

    fun getNextBorder() = if (WorldBorderService.currentSize > 100)
    {
        (WorldBorderService.currentSize - borderDecreaseAmount.value).toInt()
    } else
    {
        if (WorldBorderService.currentSize > 10)
        {
            oneHundredIntervals[indexForHundreds + 1]
        } else 10
    }

    fun calculateNextBorder()
    {
        if (WorldBorderService.currentSize > 100)
        {
            WorldBorderService.currentSize -= 100
        } else
        {
            if (WorldBorderService.currentSize > 10)
            {
                indexForHundreds++
                WorldBorderService.currentSize =
                    oneHundredIntervals[indexForHundreds]
                        .toDouble()
            }
        }
    }

    class BorderUpdateRunnable(
        seconds: Int, val next: Int
    ) : CountdownRunnable(seconds)
    {
        override fun getSeconds() = broadcastIntervals

        override fun onEnd()
        {
            broadcast(
                "$prefix${CC.GREEN}The border has shrunk to $next!"
            )

            // handles border update for
            // the current border interval
            WorldBorderService.pushSizeUpdate(next.toDouble())

            // retrieves the next border (if there is any)
            calculateNextBorder()

            // checks if this runnable is not
            // handling the last border update
            if (next != (if (flatMeetup.value) 25 else 10))
            {
                currentBorderUpdater = BorderUpdateRunnable(
                    borderShrink.value * 60,
                    getNextBorder()
                )

                currentUpdateTask?.closeAndReportException()
                currentUpdateTask = Schedulers
                    .async()
                    .runRepeating(
                        currentBorderUpdater!!,
                        0L, 20L
                    )
            } else
            {
                currentBorderUpdater = null

                currentUpdateTask?.closeAndReportException()
                currentUpdateTask = null

                if (flatMeetup.value)
                {
                    WorldBorderService.flatZone(25)
                }
            }
        }

        override fun onRun()
        {
            broadcast(
                "${CC.SEC}The border will shrink to ${CC.PRI}$next${CC.SEC} in ${CC.PRI}${
                    TimeUtil.formatIntoDetailedString(
                        seconds
                    )
                }${CC.SEC}."
            )
        }
    }
}
