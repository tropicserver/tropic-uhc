package gg.tropic.uhc.shared.player

import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.player.statistic.value.CgsGameStatistic
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
class UHCPlayerModel(uniqueId: UUID) : GameSpecificStatistics(uniqueId)
{
    private val goldenApples = CgsGameStatistic()
    private val goldenHeads = CgsGameStatistic()
    private val diamondsMined = CgsGameStatistic()
    private val goldMined = CgsGameStatistic()
    private val ironMined = CgsGameStatistic()
    private val redstoneMined = CgsGameStatistic()
    private val lapisMined = CgsGameStatistic()
    private val coalMined = CgsGameStatistic()

    override fun save() = DataStoreObjectControllerCache
        .findNotNull<UHCPlayerModel>()
        .save(this, DataStoreStorageType.MONGO)
}
