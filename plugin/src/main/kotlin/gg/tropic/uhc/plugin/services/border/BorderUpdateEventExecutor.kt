package gg.tropic.uhc.plugin.services.border

import gg.tropic.uhc.plugin.engine.CountdownRunnable
import gg.tropic.uhc.plugin.services.configurate.borderDecreaseAmount
import gg.tropic.uhc.plugin.services.configurate.borderShrink
import gg.tropic.uhc.plugin.services.configurate.firstShrink
import gg.tropic.uhc.plugin.services.configurate.firstShrinkAmount
import gg.tropic.uhc.plugin.services.configurate.flatMeetup
import gg.tropic.uhc.plugin.services.styles.prefix
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks.sync
import net.evilblock.cubed.util.time.TimeUtil
import kotlin.math.max

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
    private var indexForHundreds = 0

    fun start()
    {
        val runnable = BorderUpdateRunnable(
            firstShrink.value * 60,
            (WorldBorderService.initialSize - firstShrinkAmount.value.toDouble()).toInt(),
            overrideAutoCalculation = true
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
        max((WorldBorderService.currentSize - borderDecreaseAmount.value).toInt(), 100)
    } else
    {
        if (WorldBorderService.currentSize > 10)
        {
            oneHundredIntervals
                .getOrNull(indexForHundreds + 1)
                ?: oneHundredIntervals.last()
        } else 10
    }

    fun calculateNextBorder()
    {
        if (WorldBorderService.currentSize > 100)
        {
            WorldBorderService.currentSize -= borderDecreaseAmount.value
        } else
        {
            if (WorldBorderService.currentSize > 10)
            {
                WorldBorderService.currentSize =
                    oneHundredIntervals[indexForHundreds++]
                        .toDouble()
            }
        }
    }

    class BorderUpdateRunnable(
        seconds: Int, val next: Int, val overrideAutoCalculation: Boolean = false
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
            if (!overrideAutoCalculation)
            {
                calculateNextBorder()
            } else
            {
                WorldBorderService.currentSize = next.toDouble()
            }

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
                    sync {
                        WorldBorderService.flatZone(25)
                    }
                }
            }
        }

        private val extraInfoTimestamps = listOf(60, 30, 120, 60 * 5)

        override fun onRun()
        {
            broadcast(
                "${CC.SEC}The border will shrink to ${CC.PRI}$next${CC.SEC} in ${CC.PRI}${
                    TimeUtil.formatIntoDetailedString(
                        seconds
                    )
                }${CC.SEC}.${
                    if (seconds in extraInfoTimestamps) " Players outside of the border will be teleported within the border." else ""
                }"
            )
        }
    }
}
