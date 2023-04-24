package gg.tropic.uhc.plugin.engine.commands

import gg.scala.cgs.game.command.ForceStartCommand
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.scenario.profile
import gg.tropic.uhc.plugin.services.styles.prefix
import net.evilblock.cubed.util.CC
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
@CommandAlias("uhc")
@CommandPermission("uhc.command.uhc")
object UHCCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

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

    @AssignPermission
    @Subcommand("xray-alerts")
    @Description("Enable X-Ray alerts.")
    fun onXRayAlerts(player: ScalaPlayer)
    {
        player.bukkit().setMetadata(
            "xray-alerts",
            FixedMetadataValue(plugin, true)
        )

        player.sendMessage(
            "$prefix${CC.GREEN}You've enabled X-Ray alerts."
        )
    }

    @Subcommand("game-stats")
    @Description("View current game stats for a player.")
    fun onGameStats(player: ScalaPlayer, target: OnlinePlayer)
    {
        val model = target.player.profile
        player.sendMessage(
            "${CC.GREEN}${target.player.name}'s statistics:",
            "${CC.GRAY}Coal Mined: ${CC.GREEN}${model.coalMined.value}",
            "${CC.GRAY}Lapis Mined: ${CC.GREEN}${model.lapisMined.value}",
            "${CC.GRAY}Gold Mined: ${CC.GREEN}${model.goldMined.value}",
            "${CC.GRAY}Diamond Mined: ${CC.GREEN}${model.diamondsMined.value}",
            "${CC.GRAY}Iron Mined: ${CC.GREEN}${model.ironMined.value}",
        )
    }
}
