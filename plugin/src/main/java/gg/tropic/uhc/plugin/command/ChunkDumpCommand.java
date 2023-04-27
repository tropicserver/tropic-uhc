package gg.tropic.uhc.plugin.command;

import gg.scala.commons.acf.annotation.CommandAlias;
import gg.scala.commons.acf.annotation.CommandPermission;
import gg.scala.commons.annotations.commands.AutoRegister;
import gg.scala.commons.command.ScalaCommand;
import gg.scala.commons.issuer.ScalaPlayer;
import gg.tropic.uhc.plugin.engine.UHCGameEngine;
import net.evilblock.cubed.util.CC;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class ChunkDumpCommand extends ScalaCommand {

    @CommandAlias("chunkdump")
    @CommandPermission("uhc.command.chunkdump")
    public void onChunkDump(ScalaPlayer player)
    {
        Location loc = player.bukkit().getLocation();

        for (World world : Bukkit.getWorlds()) {
            ChunkProviderServer cps = ((CraftWorld) world).getHandle().chunkProviderServer;
            List<Chunk> chunks = new ArrayList<>(cps.chunks.values());

            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Chunk chunk : cps.chunks.values()) {
                if (chunk.locX < minX) minX = chunk.locX;
                if (chunk.locZ < minZ) minZ = chunk.locZ;
                if (chunk.locX > maxX) maxX = chunk.locX;
                if (chunk.locZ > maxZ) maxZ = chunk.locZ;
            }

            int sizeX = (maxX - minX) + 1;
            int sizeZ = (maxZ - minZ) + 1;

            boolean currentWorld = loc.getWorld().equals(world);

            Integer playerChunkX = null;
            Integer playerChunkZ = null;

            if (currentWorld) {
                playerChunkX = loc.getBlockX() >> 4;
                playerChunkZ = loc.getBlockZ() >> 4;
            }

            BufferedImage image = new BufferedImage((sizeX * 2) + 1, (sizeZ * 2) + 1, BufferedImage.TYPE_INT_RGB);

            for (Chunk chunk : chunks) {
                int x = (chunk.locX - minX) * 2;
                int z = (chunk.locZ - minZ) * 2;

                image.setRGB(x, z, currentWorld && (chunk.locX == playerChunkX && chunk.locZ == playerChunkZ) ?
                        Color.ORANGE.getRGB() : Color.GREEN.getRGB());
            }

            File dataFolder = UHCGameEngine.getINSTANCE().getPlugin().getDataFolder();

            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            try {
                player.sendMessage(CC.GREEN + "World '" + world.getName() + "' min=" + minX + ":" + minZ + " max=" + maxX + ":" + maxZ + " mid=" +
                        ((maxX - minX) + ":" + (maxZ - minZ)));
                player.sendMessage(CC.GREEN + "Exporting " + world.getName() + " chunks... (" + chunks.size() + ")");
                ImageIO.write(image, "PNG", new File(dataFolder, world.getName() + "_chunks.png"));
                player.sendMessage(CC.GREEN + "Done");
            } catch (IOException e) {
                player.sendMessage(CC.RED + "Encountered an issue :(");
                e.printStackTrace();
            }
        }
    }

}
