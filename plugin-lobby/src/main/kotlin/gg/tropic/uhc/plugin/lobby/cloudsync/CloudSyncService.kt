package gg.tropic.uhc.plugin.lobby.cloudsync

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Service
@IgnoreAutoScan
@SoftDependency("cloudsync")
object CloudSyncService
{
    @Configure
    fun configure()
    {
        CloudSyncDiscoveryService
            .discoverable.assets
            .add(
                "gg.tropic.uhc:plugin-lobby:uhc-plugin-lobby${
                    if ("dev" in ServerSync.getLocalGameServer().groups) ":gradle-dev" else ""
                }"
            )
    }
}
