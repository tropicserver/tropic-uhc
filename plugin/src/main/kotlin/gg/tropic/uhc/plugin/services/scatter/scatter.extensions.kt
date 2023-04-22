package gg.tropic.uhc.plugin.services.scatter

import gg.scala.cgs.common.CgsGameEngine
import net.minecraft.server.v1_8_R3.EntityBat
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 4/21/2023
 */
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
