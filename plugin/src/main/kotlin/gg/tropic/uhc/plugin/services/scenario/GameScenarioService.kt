package gg.tropic.uhc.plugin.services.scenario

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service

/**
 * @author GrowlyX
 * @since 4/22/2023
 */
@Service
object GameScenarioService
{
    val scenarios = mutableMapOf<String, GameScenario>()

    @Configure
    fun configure()
    {
        // require all other scenarios to be initialized
        hasteyBoys
    }
}
