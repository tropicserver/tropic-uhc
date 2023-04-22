package gg.tropic.uhc.plugin.services.configurate.menu

import gg.tropic.uhc.plugin.services.configurate.configurables
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.text.TextSplitter
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
    }

    override fun size(buttons: Map<Int, Button>) = 36

    override fun getAllPagesButtonSlots() = slots
    override fun getMaxItemsPerPage(player: Player) = slots.size

    override fun getAllPagesButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            configurables.configurables
                .forEach {
                    this[size] = ItemBuilder
                        .copyOf(it.item)
                        .name("${CC.YELLOW}${it.name}")
                        .setLore(
                            TextSplitter.split(
                                text = it.description,
                                linePrefix = CC.GRAY
                            )
                        )
                        .addToLore(
                            "",
                            "${CC.WHITE}Current value: ${CC.GOLD}${it.value}",
                            "${CC.WHITE}Default value: ${it.defaultValue}",
                        )
                        .apply {
                            if (player.hasPermission("uhc.configurate"))
                            {
                                addToLore(
                                    "",
                                    "${CC.GREEN}Click to edit!"
                                )
                            }
                        }
                        .toButton()
                }
        }

    override fun getPrePaginatedTitle(player: Player) = "Game Configuration"
}
