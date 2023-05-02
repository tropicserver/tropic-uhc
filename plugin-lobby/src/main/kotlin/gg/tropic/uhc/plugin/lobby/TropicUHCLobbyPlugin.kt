package gg.tropic.uhc.plugin.lobby

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.tropic.uhc.plugin.lobby.engine.UHCGameLobby

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
@Plugin(
    name = "UHC",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("GrowlyX")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("ScGameLobby"),
    PluginDependency("cloudsync", soft = true)
)
class TropicUHCLobbyPlugin : ExtendedScalaPlugin()
{
    @ContainerEnable
    fun containerEnable()
    {
        val lobby = UHCGameLobby()
        lobby.resourcePlugin = this
        CgsGameLobby.INSTANCE = lobby
    }
}
