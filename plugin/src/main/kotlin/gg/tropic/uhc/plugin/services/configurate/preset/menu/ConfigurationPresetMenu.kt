package gg.tropic.uhc.plugin.services.configurate.preset.menu

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.commons.acf.ConditionFailedException
import gg.tropic.uhc.plugin.services.configurate.configurables
import gg.tropic.uhc.plugin.services.configurate.preset.ConfigurationPresetService
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
class ConfigurationPresetMenu : PaginatedMenu()
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
    override fun getAllPagesButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            ConfigurationPresetService.cached()
                .presets.forEach { (_, preset) ->
                    this[size] = ItemBuilder
                        .of(Material.BOOK_AND_QUILL)
                        .name("${CC.GREEN}${preset.name}")
                        .apply {
                            addToLore("${CC.GRAY}Config Options:")

                            preset.configMappings.forEach { (t, u) ->
                                addToLore(
                                    " ${CC.GRAY}$t: ${CC.GREEN}$u"
                                )
                            }

                            addToLore(
                                "",
                                "${CC.GRAY}Scenarios:"
                            )

                            if (preset.scenariosEnabled.isEmpty())
                            {
                                addToLore("${CC.RED}None!")
                            } else
                            {
                                preset.scenariosEnabled
                                    .forEach { scenario ->
                                        addToLore("${CC.WHITE} - $scenario")
                                    }
                            }

                            addToLore(
                                "",
                                "${CC.GREEN}Left-Click to apply!",
                                "${CC.RED}Right-Click to delete!"
                            )
                        }
                        .toButton { _, type ->
                            if (type!!.isRightClick)
                            {
                                ConfirmMenu("Confirm") {
                                    if (it)
                                    {
                                        val cached = ConfigurationPresetService.cached()
                                        cached.presets.remove(preset.name)

                                        ConfigurationPresetService.sync(cached)
                                        player.sendMessage("${CC.RED}You deleted the ${preset.name} preset!")

                                        Tasks.delayed(1L) {
                                            openMenu(player)
                                        }
                                    }
                                }.openMenu(player)
                                return@toButton
                            }

                            if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
                            {
                                player.sendMessage("${CC.RED}You cannot apply presets at this time!")
                                return@toButton
                            }

                            player.closeInventory()
                            preset.apply(player)
                        }
                }
        }

    override fun getMaxItemsPerPage(player: Player) = slots.size
    override fun getPrePaginatedTitle(player: Player) = "Configuration Presets"
}
