package gg.tropic.uhc.plugin.services.configurate.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.configurate.configurables
import gg.tropic.uhc.plugin.services.configurate.menu.ConfigurateMenu
import net.evilblock.cubed.util.CC

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

    @CommandAlias("printconfig")
    fun onPrintConfig(player: ScalaPlayer)
    {
        player.sendMessage("${CC.GREEN}Game Config:")

        configurables.forEach {
            player.sendMessage(
                "${CC.GRAY}${it.name}: ${CC.GREEN}${it.value}"
            )
        }
    }
}
