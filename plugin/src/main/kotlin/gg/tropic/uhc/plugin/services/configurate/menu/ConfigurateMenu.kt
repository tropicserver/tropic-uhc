package gg.tropic.uhc.plugin.services.configurate.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.tropic.uhc.plugin.services.configurate.Configurable
import gg.tropic.uhc.plugin.services.configurate.configurables
import gg.tropic.uhc.plugin.services.hosting.isHost
import gg.tropic.uhc.plugin.services.hosting.menu.HostSetupMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
class ConfigurateMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
        )
    }

    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 36

    override fun getAllPagesButtonSlots() = slots
    override fun getMaxItemsPerPage(player: Player) = slots.size

    override fun getGlobalButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            if (!player.isHost())
            {
                return@apply
            }

            this[4] = ItemBuilder
                .of(XMaterial.GREEN_STAINED_GLASS_PANE)
                .name("${CC.GREEN}Game Setup")
                .addToLore(
                    "${CC.GRAY}Configure core game",
                    "${CC.GRAY}options such as:",
                    "${CC.WHITE}- max player count",
                    "${CC.WHITE}- team size",
                    "",
                    "${CC.GREEN}Click to open!"
                )
                .toButton { _, _ ->
                    if (!player.isHost())
                    {
                        player.sendMessage("${CC.RED}You must be the game host to use this!")
                        return@toButton
                    }

                    if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
                    {
                        player.sendMessage("${CC.RED}You cannot use this feature at this time!")
                        return@toButton
                    }

                    HostSetupMenu().openMenu(player)
                }
        }

    override fun getAllPagesButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            configurables
                .forEach {
                    this[size] = ItemBuilder
                        .copyOf(it.item)
                        .name("${CC.GREEN}${it.name}")
                        .setLore(
                            TextSplitter.split(
                                text = it.description,
                                linePrefix = CC.GRAY
                            )
                        )
                        .apply {
                            if (it.acceptedValues.isNotEmpty())
                            {
                                addToLore("")
                                addToLore("${CC.WHITE}Current value:")

                                it.acceptedValues.forEach { value ->
                                    addToLore(
                                        "${if (value == it.value) "${CC.GREEN}► " else CC.GRAY}$value"
                                    )
                                }
                            } else
                            {
                                addToLore(
                                    "",
                                    "${CC.WHITE}Current: ${CC.GREEN}${
                                        if (it.value == true) "Yes" else if (it.value == false) "${CC.RED}No" else it.value
                                    }"
                                )
                            }

                            if (player.isHost())
                            {
                                addToLore(
                                    "",
                                    "${CC.GREEN}Click to edit!"
                                )
                            }
                        }
                        .toButton { _, _ ->
                            if (!player.isHost())
                            {
                                return@toButton
                            }

                            if (it.value is Boolean)
                            {
                                it.valueInternal = !(it.value as Boolean)
                                player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)
                                return@toButton
                            }

                            if (it.acceptedValues.isNotEmpty())
                            {
                                val index = it.acceptedValues.indexOf(it.value)
                                val newValue = it.acceptedValues.toList()
                                    .getOrNull(index + 1)
                                    ?: it.acceptedValues.first()

                                it.valueInternal = newValue
                                player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)
                                return@toButton
                            }

                            if (it.value is Int)
                            {
                                player.closeInventory()

                                NumberPrompt()
                                    .withText("Enter a number:")
                                    .acceptInput { number ->
                                        it.valueInternal = number.toInt()
                                        player.playSound(player.location, Sound.NOTE_PLING, 1.0f, 1.0f)
                                        player.sendMessage("${CC.GREEN}${it.name}${CC.SEC} set to: ${CC.PRI}${number.toInt()}")

                                        openMenu(player)
                                    }
                                    .start(player)
                                return@toButton
                            }
                        }
                }
        }

    override fun getPrePaginatedTitle(player: Player) = "Game Configuration"
}
