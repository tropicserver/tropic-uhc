package gg.tropic.uhc.plugin.services.configurate.preset.commands

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
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
import gg.tropic.uhc.plugin.services.configurate.preset.ConfigurationPresetService
import gg.tropic.uhc.plugin.services.configurate.preset.menu.ConfigurationPresetMenu
import gg.tropic.uhc.plugin.services.styles.prefix
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
@AutoRegister
@CommandAlias("preset|presets")
@CommandPermission("uhc.command.preset")
object ConfigPresetCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @AssignPermission
    @Subcommand("apply")
    @Description("Apply existing presets to the current game.")
    fun onApply(player: ScalaPlayer)
    {
        ConfigurationPresetMenu().openMenu(player.bukkit())
    }

    @AssignPermission
    @Subcommand("build")
    @Description("Create a preset from your current configuration.")
    fun onBuild(player: ScalaPlayer, name: String)
    {
        if (ConfigurationPresetService.cached().presets[name] != null)
        {
            throw ConditionFailedException("A preset with that name already exists!")
        }

        val cached = ConfigurationPresetService.cached()
        cached.presets[name] = ConfigurationPresetService
            .buildPresetFromCurrentSetup(name)

        ConfigurationPresetService.sync(cached)

        player.sendMessage(
            "$prefix${CC.GREEN}A new preset with the name $name has been created!",
            "$prefix${CC.GRAY}Apply it in the future using /preset apply.",
        )
    }
}
