package gg.tropic.uhc.plugin.services.scenario.menu

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.tropic.uhc.plugin.services.hosting.isHost
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
class ScenarioMenu : PaginatedMenu()
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
    override fun getAllPagesButtons(player: Player) =
        mutableMapOf<Int, Button>().apply {
            GameScenarioService.scenarios
                .forEach { (_, scenario) ->
                    this[size] = ItemBuilder
                        .copyOf(scenario.icon)
                        .name("${CC.YELLOW}${scenario.name}")
                        .setLore(
                            TextSplitter.split(
                                text = scenario.description,
                                linePrefix = CC.GRAY
                            )
                        )
                        .addToLore(
                            "",
                            "${CC.WHITE}State: ${
                                if (scenario.enabled) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"
                            }"
                        )
                        .apply {
                            if (player.isHost())
                            {
                                addToLore("", "${CC.GREEN}Click to toggle!")
                            }
                        }
                        .toButton { _, _ ->
                            if (player.isHost())
                            {
                                if (CgsGameEngine.INSTANCE.gameState != CgsGameState.WAITING)
                                {
                                    player.sendMessage("${CC.RED}You cannot modify scenarios right now!")
                                    return@toButton
                                }

                                scenario.enabled = !scenario.enabled
                            }
                        }
                }
        }

    override fun getMaxItemsPerPage(player: Player) = slots.size

    override fun getPrePaginatedTitle(player: Player) = "Scenarios"
}
