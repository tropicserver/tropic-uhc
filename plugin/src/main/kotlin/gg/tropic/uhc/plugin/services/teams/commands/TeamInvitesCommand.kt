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
object TeamInvitesCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    @CommandAlias("toggleteaminvites|teaminvites|tivs")
    @Conditions("team-game-required")
    fun onTeamInvites(player: ScalaPlayer)
    {
        if (player.bukkit().hasMetadata("teaminvites-disabled"))
        {
            player.bukkit().removeMetadata("teaminvites-disabled", plugin)
            player.bukkit().sendMessage("${CC.GREEN}You are now able to receive team invite requests!")
            return
        }

        player.bukkit().setMetadata("teaminvites-disabled", FixedMetadataValue(plugin, true))
        player.bukkit().sendMessage("${CC.RED}You are no longer able to receive team invites.")
    }
}
