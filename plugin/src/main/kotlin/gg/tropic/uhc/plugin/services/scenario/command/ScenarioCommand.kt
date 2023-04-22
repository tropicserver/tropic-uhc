package gg.tropic.uhc.plugin.services.scenario.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.configurate.menu.ConfigurateMenu
import gg.tropic.uhc.plugin.services.scenario.menu.ScenarioMenu

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object ScenarioCommand : ScalaCommand()
{
    @CommandAlias("scenarios")
    fun onScenarios(player: ScalaPlayer)
    {
        ScenarioMenu().openMenu(player.bukkit())
    }
}
