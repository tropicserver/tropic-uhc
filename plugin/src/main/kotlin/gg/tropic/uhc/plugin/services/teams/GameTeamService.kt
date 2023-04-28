package gg.tropic.uhc.plugin.services.teams

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelService
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.teams.channel.TeamChatChannelComposite
import me.lucko.helper.utils.Players
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 4/26/2023
 */
@Service
object GameTeamService
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    fun configureTeamResources()
    {
        CgsGameTeamService.teams.clear()

        for (id in 1..((Bukkit.getMaxPlayers() / gameType.teamSize) + 1))
        {
            CgsGameTeamService.teams[id] =
                CgsGameTeamService.engine.createTeam(id)

            val channel = ChatChannelBuilder.newBuilder()
                .import(TeamChatChannelComposite)
                .compose()
                .monitor()
                .allowOnlyIf {
                    it.team?.id == id
                }
                .override(11) {
                    it.hasMetadata("teamchat")
                }

            ChatChannelService.register(channel)
        }

        Players.all()
            .forEach {
                CgsGameTeamService
                    .allocatePlayersToAvailableTeam(
                        player = CgsPlayerHandler.find(it)!!
                    )
            }
    }
}
