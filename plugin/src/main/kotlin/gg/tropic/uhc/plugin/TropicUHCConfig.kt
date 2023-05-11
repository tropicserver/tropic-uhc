package gg.tropic.uhc.plugin

import xyz.mkotb.configapi.comment.Comment

/**
 * @author GrowlyX
 * @since 5/3/2023
 */
class TropicUHCConfig
{
    @Comment("The following settings are related to the autonomous plugin mode.")
    var autonomous = false
    var autonomousPreGenWorldFolder = "/opt/glade/worlds/"
    var autonomousWorldBorderSize = 1000

    var autonomousGameType = "FFA"
    var autonomousMaxPlayers = 80
    var autonomousInitialHealth = 40.0
}
