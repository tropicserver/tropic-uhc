package gg.tropic.uhc.plugin.services.border

import gg.scala.lemon.util.task.DiminutionRunnable
import gg.tropic.uhc.plugin.services.configurate.borderShrink
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil

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

    private val oneHundredIntervals = arrayOf(50, 25, 10)
    private var indexForHundreds = -1

    fun start()
    {
        val runnable = BorderUpdateRunnable(
            borderShrink.value * 60,
            WorldBorderService.currentSize.toInt()
        )

        currentBorderUpdater = runnable
        currentUpdateTask = Schedulers
            .async()
            .runRepeating(
                runnable,
                0L, 20L
            )
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
    ) : DiminutionRunnable(seconds)
    {
        override fun getSeconds(): List<Int>
        {
            return broadcastIntervals
        }

        override fun onEnd()
        {
            broadcast(
                "${CC.SEC}The border has shrunk to ${CC.PRI}$next${CC.SEC}."
            )

            // handles border update for
            // the current border interval
            WorldBorderService.pushSizeUpdate(next.toDouble())

            // retrieves the next border (if there is any)
            calculateNextBorder()

            // checks if this runnable is not
            // handling the last border update
            if (WorldBorderService.currentSize.toInt() != next)
            {
                currentBorderUpdater = BorderUpdateRunnable(
                    borderShrink.value * 60,
                    WorldBorderService.currentSize.toInt()
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
