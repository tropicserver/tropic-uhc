package gg.tropic.uhc.plugin

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginWebsite
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.tropic.uhc.plugin.engine.UHCGameEngine

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@Plugin(
    name = "UHC",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("ScGameFramework"),
    PluginDependency("features"),
    PluginDependency("cloudsync", soft = true)
)
class TropicUHCPlugin : ExtendedScalaPlugin()
{
    @ContainerEnable
    fun containerEnable()
    {
        val engine = UHCGameEngine(this)
        engine.initialLoad()

        flavor {
            bind<UHCGameEngine>() to engine
        }
    }
}
