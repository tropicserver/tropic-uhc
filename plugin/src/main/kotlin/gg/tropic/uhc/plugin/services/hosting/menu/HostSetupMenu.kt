package gg.tropic.uhc.plugin.services.hosting.menu

import gg.tropic.uhc.plugin.services.teams.GameTeamType
import gg.tropic.uhc.plugin.services.teams.allowGameTypeEditing
import gg.tropic.uhc.plugin.services.teams.compatibleWith
import gg.tropic.uhc.plugin.services.teams.gameType
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/25/2023
 */
class HostSetupMenu : Menu("Game Setup")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            this[3] = ItemBuilder
                .of(Material.FIREBALL)
                .name("${CC.GREEN}Max Player Count")
                .addToLore(
                    "${CC.GRAY}Set the max player count",
                    "${CC.GRAY}for this UHC game.",
                    "",
                    "${CC.WHITE}Current: ${CC.GREEN}${
                        Bukkit.getMaxPlayers()
                    }",
                    "",
                    "${CC.GREEN}Click to change!"
                )
                .toButton { _, _ ->
                    player.closeInventory()

                    NumberPrompt()
                        .withText("${CC.GREEN}Enter a player count:")
                        .acceptInput {
                            if (!gameType.compatibleWith(it.toInt()))
                            {
                                player.sendMessage("${CC.RED}${it.toInt()} does not work as a max player count for ${gameType.name}!")
                                openMenu(player)
                                return@acceptInput
                            }

                            Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "setmaxplayers ${it.toInt()}"
                            )

                            player.sendMessage(
                                "${CC.SEC}Set max players to: ${CC.PRI}${it.toInt()}"
                            )
                            openMenu(player)
                        }
                        .start(player)
                }

            this[5] = ItemBuilder
                .of(Material.FIREBALL)
                .name("${CC.GREEN}Game Mode")
                .addToLore(
                    "${CC.GRAY}Set the game team mode",
                    "${CC.GRAY}for this UHC game.",
                    "",
                )
                .apply {
                    GameTeamType.values().forEach {
                        addToLore("${if (gameType == it) "${CC.GREEN}► " else CC.GRAY}${it.name}")
                    }
                }
                .addToLore(
                    "",
                    "${CC.GREEN}Click to change!"
                )
                .toButton { _, _ ->
                    if (!allowGameTypeEditing)
                    {
                        player.sendMessage("${CC.RED}Other game types are not yet available!")
                        return@toButton
                    }

                    gameType = GameTeamType
                        .values()
                        .getOrNull(gameType.ordinal + 1)
                        ?: GameTeamType.FFA

                    player.playSound(
                        player.location,
                        Sound.NOTE_PLING,
                        1.0f, 1.0f
                    )
                    openMenu(player)
                }
        }
}
