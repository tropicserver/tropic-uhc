package gg.tropic.uhc.plugin.services.teams.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.tropic.uhc.plugin.TropicUHCPlugin
import net.evilblock.cubed.util.CC
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 4/27/2023
 */
@AutoRegister
object TeamChatCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    @CommandAlias("teamchat|tc")
    @Conditions("team-game-required")
    fun onTeamChat(player: ScalaPlayer)
    {
        if (player.bukkit().hasMetadata("teamchat"))
        {
            player.bukkit().removeMetadata("teamchat", plugin)
            player.bukkit().sendMessage("${CC.RED}You are no longer chatting in the team channel.")
            return
        }

        player.bukkit().setMetadata("teamchat", FixedMetadataValue(plugin, true))
        player.bukkit().sendMessage("${CC.GREEN}You are now chatting in the team channel.")
    }
}
