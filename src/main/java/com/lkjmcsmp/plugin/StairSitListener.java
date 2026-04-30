package com.lkjmcsmp.plugin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StairSitListener implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Seat> seatsByPlayer = new ConcurrentHashMap<>();
    private final Map<SeatKey, UUID> playersBySeat = new ConcurrentHashMap<>();

    public StairSitListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || !(block.getBlockData() instanceof Stairs)) {
            return;
        }
        if (player.isSneaking() || !player.hasPermission("lkjmcsmp.sit.stairs")) {
            return;
        }
        if (!player.getInventory().getItemInMainHand().getType().isAir()) {
            return;
        }
        event.setCancelled(true);
        sit(player, block);
    }

    private void sit(Player player, Block block) {
        SeatKey key = SeatKey.of(block);
        if (seatsByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage("You are already sitting.");
            return;
        }
        if (playersBySeat.containsKey(key)) {
            player.sendMessage("That stair is already occupied.");
            return;
        }
        Location location = block.getLocation().add(0.5D, 0.2D, 0.5D);
        ArmorStand stand = block.getWorld().spawn(location, ArmorStand.class, this::configureSeat);
        stand.addPassenger(player);
        seatsByPlayer.put(player.getUniqueId(), new Seat(key, stand));
        playersBySeat.put(key, player.getUniqueId());
    }

    private void configureSeat(ArmorStand stand) {
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setBasePlate(false);
        stand.setPersistent(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            cleanup(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cleanup(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID playerId = playersBySeat.get(SeatKey.of(event.getBlock()));
        if (playerId != null) {
            cleanup(playerId);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            for (UUID playerId : java.util.List.copyOf(seatsByPlayer.keySet())) {
                cleanup(playerId);
            }
        }
    }

    private void cleanup(UUID playerId) {
        Seat seat = seatsByPlayer.remove(playerId);
        if (seat == null) {
            return;
        }
        playersBySeat.remove(seat.key());
        if (!seat.stand().isDead()) {
            seat.stand().eject();
            seat.stand().remove();
        }
    }

    private record Seat(SeatKey key, ArmorStand stand) {
    }

    private record SeatKey(String world, int x, int y, int z) {
        static SeatKey of(Block block) {
            return new SeatKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }
    }
}
