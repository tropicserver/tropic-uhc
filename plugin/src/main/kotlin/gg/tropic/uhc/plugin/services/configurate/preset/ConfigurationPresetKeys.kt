package gg.tropic.uhc.plugin.services.configurate.preset

import gg.scala.commons.persist.datasync.DataSyncKeys

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
object ConfigurationPresetKeys : DataSyncKeys
{
    override fun newStore() = "uhc-presets"

    override fun store() = keyOf("uhcpresets", "configuration")
    override fun sync() = keyOf("uhcpresets", "update")
}
