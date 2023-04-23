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
    val diamondsMined = CgsGameStatistic()
    val goldMined = CgsGameStatistic()
    val ironMined = CgsGameStatistic()
    val redstoneMined = CgsGameStatistic()
    val spawnersMined = CgsGameStatistic()
    val lapisMined = CgsGameStatistic()
    val coalMined = CgsGameStatistic()

    var limGold = 0
    var limDiamond = 0
    var limIron = 0

    override fun save() = DataStoreObjectControllerCache
        .findNotNull<UHCPlayerModel>()
        .save(this, DataStoreStorageType.MONGO)
}
