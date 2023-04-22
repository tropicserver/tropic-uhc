package gg.tropic.uhc.plugin.services.scenario

import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
abstract class GameScenario(
    val name: String,
    val icon: ItemStack,
    val description: String
) : Listener
{
    var enabled: Boolean = false

    init
    {
        GameScenarioService.scenarios[name] = this
    }

    open fun configure()
    {

    }

    open fun preDestroy()
    {

    }
}
