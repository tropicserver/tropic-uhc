package gg.tropic.uhc.plugin.engine.commands

import gg.scala.cgs.game.command.ForceStartCommand
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.tropic.uhc.plugin.services.configurate.initialBorderSize
import org.bukkit.Bukkit
import java.io.File

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
@CommandAlias("uhc")
@CommandPermission("uhc.command.uhc")
object UHCCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @AssignPermission
    @Subcommand("start")
    @Description("Start the UHC game with your current configuration & scenarios.")
    fun onStart(player: ScalaPlayer) = ForceStartCommand
        .onForceStart(player.bukkit())
}
