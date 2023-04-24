package gg.tropic.uhc.plugin.services.map.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
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
object ChunkGenUnlockCommand : ScalaCommand()
{
    @CommandAlias("reset-overworld")
    @CommandPermission("uhc.command.reset-overworld")
    fun onUnlock(player: ScalaPlayer)
    {
        File(Bukkit.getWorldContainer(), "tropic.uhc.lock").delete()
        player.sendMessage("Unlocked world gen. Restart for a newly generated world!")
    }

    @CommandAlias("lock-gen-border")
    @CommandPermission("op")
    fun onLockGenBorder(player: ScalaPlayer)
    {
        File(Bukkit.getWorldContainer(), "tropic.uhc.lock")
            .apply {
                if (!exists())
                    createNewFile()

                writeText(initialBorderSize.value.toString())

                player.sendMessage(
                    "Locked chunk gen to ${initialBorderSize.value} border size"
                )
            }
    }
}
