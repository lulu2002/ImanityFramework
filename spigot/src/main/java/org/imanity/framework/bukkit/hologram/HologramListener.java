package org.imanity.framework.bukkit.hologram;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.TaskUtil;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

import java.util.HashSet;
import java.util.Set;

public class HologramListener implements Listener {

    private final Set<Player> toUpdate = new HashSet<>();

    public HologramListener() {
        iSpigot.INSTANCE.addMovementHandler(new MovementHandler() {
            @Override
            public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
                if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
                    return;
                }

                toUpdate.add(player);
            }

            @Override
            public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {}
        });

        TaskUtil.runAsyncRepeated(() -> {
            if (Imanity.SHUTTING_DOWN) {
                return;
            }
            toUpdate.removeIf(player -> {
                this.update(player);
                return true;
            });
        }, 20 * 5L);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom(), to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        this.update(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.update(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.toUpdate.remove(player);
        Imanity.getHologramHandler(player.getWorld()).reset(player);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        event.getWorld().setMetadata(HologramHandler.WORLD_METADATA, new SampleMetadata(new HologramHandler()));
    }

    private void update(Player player) {
        Imanity.getHologramHandler(player.getWorld())
                .update(player);
    }

}