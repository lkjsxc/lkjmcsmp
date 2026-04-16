package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.plugin.Locations;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class WarpCommand implements CommandExecutor {
    private final WarpService warpService;
    private final SchedulerBridge schedulerBridge;
    private final ProgressionService progressionService;

    public WarpCommand(WarpService warpService, SchedulerBridge schedulerBridge, ProgressionService progressionService) {
        this.warpService = warpService;
        this.schedulerBridge = schedulerBridge;
        this.progressionService = progressionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (label.toLowerCase()) {
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
        schedulerBridge.runPlayerTask(player, () -> player.teleport(location.get()));
        progressionService.increment(player.getUniqueId(), "warp_use", 1);
        player.sendMessage("Teleported to warp.");
    }
}
