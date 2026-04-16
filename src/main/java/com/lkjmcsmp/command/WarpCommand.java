package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.plugin.Locations;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class WarpCommand implements CommandExecutor {
    private final WarpService warpService;
    private final TeleportService teleportService;
    private final ProgressionService progressionService;

    public WarpCommand(WarpService warpService, TeleportService teleportService, ProgressionService progressionService) {
        this.warpService = warpService;
        this.teleportService = teleportService;
        this.progressionService = progressionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (command.getName().toLowerCase()) {
                    case "setwarp" -> {
                        if (!CommandUtil.requirePermission(player, "lkjmcsmp.warp.manage")) {
                            return true;
                        }
                        if (args.length == 0) {
                            player.sendMessage("Usage: /setwarp <name>");
                            return true;
                        }
                        player.sendMessage(warpService.setWarp(player, args[0]).message());
                    }
                    case "delwarp" -> {
                        if (!CommandUtil.requirePermission(player, "lkjmcsmp.warp.manage")) {
                            return true;
                        }
                        if (args.length == 0) {
                            player.sendMessage("Usage: /delwarp <name>");
                            return true;
                        }
                        player.sendMessage(warpService.deleteWarp(args[0]).message());
                    }
                    case "warps" -> player.sendMessage("Warps: " + warpService.list().stream().map(w -> w.name()).toList());
                    case "warp" -> teleportWarp(player, args);
                    default -> {
                        return false;
                    }
                }
            } catch (Exception ex) {
                player.sendMessage("Warp command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void teleportWarp(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length == 0) {
            player.sendMessage("Usage: /warp <name>");
            return;
        }
        var warp = warpService.findWarp(args[0]);
        if (warp.isEmpty()) {
            player.sendMessage("Warp not found.");
            return;
        }
        var location = Locations.toBukkit(warp.get());
        if (location.isEmpty()) {
            player.sendMessage("World is unavailable for that warp.");
            return;
        }
        teleportService.teleportToLocation(player, location.get(), "Teleported to warp.", result -> {
            if (result.success()) {
                try {
                    progressionService.increment(player.getUniqueId(), "warp_use", 1);
                } catch (Exception ex) {
                    player.sendMessage("Progression update failed: " + ex.getMessage());
                }
            }
            player.sendMessage(result.message());
        });
    }
}
