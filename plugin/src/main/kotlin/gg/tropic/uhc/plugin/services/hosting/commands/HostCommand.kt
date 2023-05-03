package gg.tropic.uhc.plugin.services.hosting.commands

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Conditions
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.util.QuickAccess.username
import gg.tropic.uhc.plugin.services.hosting.HostService
import gg.tropic.uhc.plugin.services.hosting.hostDisplayName
import gg.tropic.uhc.plugin.services.hosting.isHost
import gg.tropic.uhc.plugin.services.styles.prefix
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object HostCommand : ScalaCommand()
{
    @CommandAlias("host-unbind")
    @Conditions("hosted-game-required")
    @CommandPermission("uhc.command.host.unbind")
    fun onHostUnbind(player: ScalaPlayer)
    {
        if (HostService.gameHost == null)
        {
            throw ConditionFailedException("There is no host to unbind.")
        }

        val current = HostService.gameHost!!
        HostService.gameHost = null

        Bukkit.broadcastMessage(
            "$prefix${CC.B_RED}${current.username()}${CC.RED} is no longer the game host!"
        )
    }

    @CommandAlias("host")
    @Conditions("hosted-game-required")
    @CommandPermission("uhc.command.host")
    fun onHost(player: ScalaPlayer)
    {
        if (player.bukkit().isHost())
        {
            throw ConditionFailedException("You're already the game host!")
        }

        if (HostService.gameHost != null)
        {
            throw ConditionFailedException(
                "${hostDisplayName()}${CC.RED} is already hosting this game!"
            )
        }

        HostService.gameHost = player.uniqueId
        player.sendMessage(
            "$prefix${CC.GREEN}You're now the game's host.",
            "$prefix${CC.GRAY}Use /spectate to enter spectator mode before the game/scatter starts.",
        )

        Bukkit.broadcastMessage(
            "$prefix${CC.B_GREEN}${player.bukkit().name}${CC.GREEN} is now the host."
        )
    }
}
