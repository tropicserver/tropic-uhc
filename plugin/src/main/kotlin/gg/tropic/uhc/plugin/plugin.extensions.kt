package gg.tropic.uhc.plugin

/**
 * @author GrowlyX
 * @since 5/3/2023
 */
val autonomous: Boolean
    get() = TropicUHCPlugin.instance
        .config<TropicUHCConfig>()
        .autonomous
