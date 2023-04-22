package gg.tropic.uhc.plugin.services.configurate.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.configurate.menu.ConfigurateMenu

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object ConfigurateCommand : ScalaCommand()
{
    @CommandAlias("config|cfg")
    fun onConfig(player: ScalaPlayer)
    {
        ConfigurateMenu().openMenu(player.bukkit())
    }
}
