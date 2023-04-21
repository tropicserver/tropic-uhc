package gg.tropic.uhc.plugin.services.styles

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
fun buildStyledPrefix(prefix: String, color: String = CC.GOLD) =
    "$color${CC.BOLD}$prefix ${CC.B_GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.RESET}"

val prefix = buildStyledPrefix(prefix = "UHC")
