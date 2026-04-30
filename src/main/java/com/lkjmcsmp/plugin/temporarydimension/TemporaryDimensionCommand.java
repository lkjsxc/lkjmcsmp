package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.PointsService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class TemporaryDimensionCommand implements CommandExecutor {
    private final PointsService pointsService;
    private final TemporaryDimensionManager manager;

    public TemporaryDimensionCommand(PointsService pointsService, TemporaryDimensionManager manager) {
        this.pointsService = pointsService;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /tempdim purchase | /tempdim list | /tempdim info <id> | /tempdim forceclose <id>");
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "purchase" -> handlePurchase(sender);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "forceclose" -> handleForceClose(sender, args);
            default -> {
                sender.sendMessage("Unknown subcommand.");
                yield true;
            }
        };
    }

    private boolean handlePurchase(CommandSender sender) {
        var opt = com.lkjmcsmp.command.CommandUtil.requirePlayer(sender);
        if (opt.isEmpty()) return true;
        Player player = opt.get();
        if (!player.hasPermission("lkjmcsmp.temporarydimension.use")) {
            player.sendMessage("Missing permission.");
            return true;
        }
        try {
            var result = pointsService.purchase(player, "temporary_dimension_pass", 1,
                    finalResult -> player.sendMessage(finalResult.message()));
            if (!result.success()) {
                player.sendMessage(result.message());
                return true;
            }
        } catch (Exception ex) {
            player.sendMessage("Purchase failed: " + ex.getMessage());
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!com.lkjmcsmp.command.CommandUtil.requirePermission(sender, "lkjmcsmp.temporarydimension.admin")) return true;
        var instances = manager.activeInstances();
        if (instances.isEmpty()) {
            sender.sendMessage("No active temporary dimension instances.");
            return true;
        }
        sender.sendMessage("Active temporary dimension instances:");
        for (var instance : instances) {
            long remaining = Duration.between(java.time.Instant.now(), instance.expirationTime()).toMinutes();
            sender.sendMessage(instance.instanceId().substring(0, 8) + " | " + instance.worldName()
                    + " | " + instance.environment() + " | " + instance.state() + " | " + Math.max(0, remaining) + "m left");
        }
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!com.lkjmcsmp.command.CommandUtil.requirePermission(sender, "lkjmcsmp.temporarydimension.admin")) return true;
        if (args.length < 2) {
            sender.sendMessage("Usage: /tempdim info <id>");
            return true;
        }
        var instance = manager.findInstance(args[1]);
        if (instance == null) {
            sender.sendMessage("Instance not found.");
            return true;
        }
        long remaining = Duration.between(java.time.Instant.now(), instance.expirationTime()).toMinutes();
        sender.sendMessage("ID: " + instance.instanceId());
        sender.sendMessage("World: " + instance.worldName());
        sender.sendMessage("Environment: " + instance.environment());
        sender.sendMessage("State: " + instance.state());
        sender.sendMessage("Remaining: " + Math.max(0, remaining) + " minutes");
        return true;
    }

    private boolean handleForceClose(CommandSender sender, String[] args) {
        if (!com.lkjmcsmp.command.CommandUtil.requirePermission(sender, "lkjmcsmp.temporarydimension.admin")) return true;
        if (args.length < 2) {
            sender.sendMessage("Usage: /tempdim forceclose <id>");
            return true;
        }
        var instance = manager.findInstance(args[1]);
        if (instance == null) {
            sender.sendMessage("Instance not found.");
            return true;
        }
        manager.expireInstance(instance.instanceId());
        sender.sendMessage("Instance " + instance.instanceId() + " forced to close.");
        return true;
    }
}
