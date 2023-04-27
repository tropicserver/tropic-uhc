package gg.tropic.uhc.plugin.engine.commands

import gg.scala.cgs.common.player.statistic.value.CgsGameStatistic
import gg.scala.cgs.game.command.ForceStartCommand
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.tropic.uhc.plugin.TropicUHCPlugin
import gg.tropic.uhc.plugin.services.map.mapWorld
import gg.tropic.uhc.plugin.services.scatter.remainingPlayers
import gg.tropic.uhc.plugin.services.scenario.playing
import gg.tropic.uhc.plugin.services.scenario.profile
import gg.tropic.uhc.plugin.services.styles.prefix
import gg.tropic.uhc.shared.player.UHCPlayerModel
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.Location
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
@AutoRegister
@CommandAlias("uhc")
@CommandPermission("uhc.command.uhc")
object UHCCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: TropicUHCPlugin

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @AssignPermission
    @Subcommand("start")
    @Description("Start the UHC game with your current configuration & scenarios.")
    fun onStart(player: ScalaPlayer) = ForceStartCommand
        .onForceStart(player.bukkit())

    @Subcommand("xray-alerts")
    @Description("Enable X-Ray alerts.")
    fun onXRayAlerts(player: ScalaPlayer)
    {
        player.bukkit().setMetadata(
            "xray-alerts",
            FixedMetadataValue(plugin, true)
        )

        player.sendMessage(
            "$prefix${CC.GREEN}You've enabled X-Ray alerts."
        )
    }

    @Subcommand("world-preview")
    @Description("Preview the UHC game's world.")
    fun onWorldPreview(player: ScalaPlayer)
    {
        player.teleport(
            Location(
                mapWorld(),
                0.500, 100.0, 0.500
            )
        )
        player.sendMessage(
            "$prefix${CC.GREEN}You were teleported to the UHC world!"
        )
    }

    enum class TopCategory(
        val sortValue: (UHCPlayerModel) -> CgsGameStatistic
    )
    {
        Coal(UHCPlayerModel::coalMined),
        Lapis(UHCPlayerModel::lapisMined),
        Gold(UHCPlayerModel::goldMined),
        Diamonds(UHCPlayerModel::diamondsMined),
        Iron(UHCPlayerModel::ironMined),
        Spawners(UHCPlayerModel::spawnersMined),
        Kills(UHCPlayerModel::gameKills)
    }

    @Subcommand("player top")
    @Description("View top 10 leaderboards for certain statistics.")
    fun onPlayerTop(player: ScalaPlayer, category: TopCategory)
    {
        val topTenPlayers = remainingPlayers
            .sortedByDescending {
                category.sortValue(it.profile).value
            }
            .take(10)

        player.sendMessage(
            "${CC.GREEN}Top 10 leaderboards for ${category.name}:",
            *topTenPlayers
                .mapIndexed { index, topPlayer ->
                    "${CC.GREEN}#${index + 1} ${CC.GRAY}- ${CC.WHITE}${topPlayer.name} ${CC.GRAY}- ${CC.WHITE}${
                        Numbers.format(category.sortValue(topPlayer.profile).value)
                    }"
                }
                .toTypedArray()
        )
    }

    @Subcommand("player stats")
    @Description("View game stats for a player.")
    fun onPlayerStats(player: ScalaPlayer, target: OnlinePlayer)
    {
        val model = target.player.profile
        player.sendMessage(
            "${CC.GREEN}${target.player.name}'s game statistics:",
            "${CC.GRAY}Coal Mined: ${CC.GREEN}${model.coalMined.value}",
            "${CC.GRAY}Lapis Mined: ${CC.GREEN}${model.lapisMined.value}",
            "${CC.GRAY}Gold Mined: ${CC.GREEN}${model.goldMined.value}",
            "${CC.GRAY}Diamond Mined: ${CC.GREEN}${model.diamondsMined.value}",
            "${CC.GRAY}Iron Mined: ${CC.GREEN}${model.ironMined.value}",
            "${CC.GRAY}Spawners Mined: ${CC.GREEN}${model.spawnersMined.value}",
            "",
            "${CC.GRAY}Game Kills: ${CC.GREEN}${model.gameKills.value}"
        )

        if (!target.player.playing)
        {
            player.sendMessage("${CC.D_GRAY}(this player is a spectator)")
        }
    }
}
