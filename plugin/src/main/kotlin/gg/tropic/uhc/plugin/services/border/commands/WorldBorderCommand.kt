package gg.tropic.uhc.plugin.services.border.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
object WorldBorderCommand : ScalaCommand()
{
    @CommandAlias("wb-testing")
    fun onWbTesting(player: ScalaPlayer)
    {
        val worldBorder = player.bukkit().world.worldBorder
        worldBorder.setCenter(
            player.bukkit().location.x,
            player.bukkit().location.z
        )

        worldBorder.damageAmount = 0.5
        worldBorder.damageBuffer = 3.5

        worldBorder.setSize(50.0, 5L)
        player.sendMessage("set border")
    }
}
