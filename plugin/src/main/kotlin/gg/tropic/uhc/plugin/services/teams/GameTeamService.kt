package gg.tropic.uhc.plugin.services.teams

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelService
import gg.tropic.uhc.plugin.services.teams.channel.TeamChatChannelComposite

/**
 * @author GrowlyX
 * @since 4/26/2023
 */
@Service
object GameTeamService
{
    @Configure
    fun configure()
    {
        val channel = ChatChannelBuilder.newBuilder()
            .import(TeamChatChannelComposite)
            .compose()
            .monitor()
            .allowOnlyIf {
                it.hasMetadata("teamchat")
            }
            .override(11) {
                it.hasMetadata("teamchat")
            }

        ChatChannelService.register(channel)
    }
}
