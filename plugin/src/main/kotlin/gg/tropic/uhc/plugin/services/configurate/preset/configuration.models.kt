package gg.tropic.uhc.plugin.services.configurate.preset

import gg.tropic.uhc.plugin.services.configurate.configurables
import gg.tropic.uhc.plugin.services.scenario.GameScenarioService
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
data class ConfigurationPreset(
    val name: String,
    val configMappings: Map<String, Any>,
    val scenariosEnabled: Set<String>
)
{
    fun apply(player: Player)
    {
        configMappings.forEach { (t, u) ->
            val match = configurables
                .firstOrNull { it.name == t }
                ?: return@forEach

            match.valueInternal = u
            player.sendMessage(
                "${CC.SEC}Set ${CC.GREEN}$t${CC.SEC}: ${CC.SEC}$u"
            )
        }

        scenariosEnabled.forEach {
            GameScenarioService.scenarios[it]?.apply {
                this.enabled = true
                player.sendMessage("${CC.SEC}Enabled scenario ${CC.GREEN}$name${CC.SEC}!")
            }
        }

        player.sendMessage("${CC.B_GREEN}Preset application complete!")
    }
}

data class ConfigurationPresetContainer(
    val presets: MutableMap<String, ConfigurationPreset> = mutableMapOf()
)
