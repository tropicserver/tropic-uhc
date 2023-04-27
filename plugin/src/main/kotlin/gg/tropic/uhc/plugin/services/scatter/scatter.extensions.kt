package gg.tropic.uhc.plugin.services.scatter

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.teams.CgsGameTeam
import gg.tropic.uhc.plugin.services.map.MapGenerationService
import gg.tropic.uhc.plugin.services.scatter.ScatterService.scatter
import me.lucko.helper.utils.Players
import net.minecraft.server.v1_8_R3.EntityBat
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
val remainingPlayers: List<Player>
    get() = Players.all()
        .filter {
            !it.hasMetadata("spectator")
        }

val CgsGameTeam.alivePlayers: List<Player>
    get() = alive
        .mapNotNull {
            Bukkit.getPlayer(it)
        }

fun CgsGameTeam.scatter()
{
    val scatterLocation = MapGenerationService
        .generateScatterLocation()

    alivePlayers.forEach {
        it.scatter(scatterLocation = scatterLocation)
    }
}

fun Player.resetAttributes()
{
    health = 20.0
    foodLevel = 20
    saturation = 12.8f
    maximumNoDamageTicks = 20
    fireTicks = 0
    fallDistance = 0.0f
    level = 0
    exp = 0.0f
    walkSpeed = 0.2f
    inventory.heldItemSlot = 0
    allowFlight = false

    inventory.clear()
    inventory.armorContents = null

    closeInventory()

    gameMode = GameMode.SURVIVAL

    activePotionEffects
        .map(PotionEffect::getType)
        .forEach { type ->
            player.removePotionEffect(type)
        }

    (this as CraftPlayer).handle.dataWatcher.watch(9, 0.toByte())
    updateInventory()
}

infix fun Player.sit(boolean: Boolean)
{
    if (boolean)
    {
        sitPlayer(this)
    } else
    {
        unsitPlayer(this)
    }
}

fun sitPlayer(player: Player)
{
    val craftPlayer = player as CraftPlayer
    val location = player.getLocation()

    val bat = EntityBat((location.world as CraftWorld).handle)
    bat.setLocation(location.x, location.y + 0.5, location.z, 0f, 0f)
    bat.isInvisible = true
    bat.health = 6f

    val spawnEntityPacket = PacketPlayOutSpawnEntityLiving(bat)
    craftPlayer.handle.playerConnection.sendPacket(spawnEntityPacket)

    player.setMetadata(
        "seated",
        FixedMetadataValue(CgsGameEngine.INSTANCE.plugin, bat.id)
    )

    val sitPacket = PacketPlayOutAttachEntity(0, craftPlayer.handle, bat)
    craftPlayer.handle.playerConnection.sendPacket(sitPacket)
}

fun unsitPlayer(player: Player)
{
    if (player.hasMetadata("seated"))
    {
        val craftPlayer = player as CraftPlayer
        val packet = PacketPlayOutEntityDestroy(player.getMetadata("seated")[0].asInt())
        craftPlayer.handle.playerConnection.sendPacket(packet)
    }
}
