package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.TeleportService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class TeleportCommand implements CommandExecutor {
    private final TeleportService teleportService;

    public TeleportCommand(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (command.getName().toLowerCase(Locale.ROOT)) {
                    case "tp" -> directTp(player, args);
                    case "tpa" -> requestTp(player, args, false);
                    case "tpahere" -> requestTp(player, args, true);
                    case "tpaccept" -> acceptRequest(player);
                    case "tpdeny" -> denyRequest(player);
                    case "rtp" -> {
                        if (!CommandUtil.requirePermission(player, "lkjmcsmp.rtp.use")) {
                            return true;
                        }
                        boolean bypass = player.hasPermission("lkjmcsmp.rtp.bypasscooldown");
                        String world = args.length > 0 ? args[0] : "world";
                        teleportService.randomTeleport(player, world, bypass, result -> player.sendMessage(result.message()));
                    }
                    default -> {
                        return false;
                    }
                }
            } catch (Exception ex) {
                player.sendMessage("Teleport command failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void directTp(Player player, String[] args) {
        if (!CommandUtil.requirePermission(player, "lkjmcsmp.tp.use")) {
            return;
        }
        if (args.length == 0) {
            player.sendMessage("Usage: /tp <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("Target offline.");
            return;
        }
        teleportService.directTeleport(player, target, result -> player.sendMessage(result.message()));
    }

    private void requestTp(Player player, String[] args, boolean summonHere) {
        if (!CommandUtil.requirePermission(player, "lkjmcsmp.tpa.use")) {
            return;
        }
        if (args.length == 0) {
            player.sendMessage("Usage: " + (summonHere ? "/tpahere <player>" : "/tpa <player>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("Target offline.");
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("Cannot target yourself.");
            return;
        }
        var result = teleportService.requestTeleport(player, target, summonHere);
        player.sendMessage(result.success() ? "Request sent to " + target.getName() + "." : result.message());
        if (!result.success()) {
            return;
        }
        String timeout = teleportService.requestTimeoutSeconds() + "s";
        String direction = summonHere
                ? player.getName() + " requested that you teleport to them."
                : player.getName() + " requested to teleport to you.";
        target.sendMessage(direction + " Use /tpaccept or /tpdeny. Expires in " + timeout + ".");
    }

    private void acceptRequest(Player player) {
        if (!CommandUtil.requirePermission(player, "lkjmcsmp.tpa.use")) {
            return;
        }
        var pending = teleportService.pendingFor(player.getUniqueId());
        teleportService.acceptRequest(player, result -> {
            player.sendMessage(result.message());
            pending.ifPresent(request -> notifyRequester(player, request.from(), result, "accepted"));
        });
    }

    private void denyRequest(Player player) {
        if (!CommandUtil.requirePermission(player, "lkjmcsmp.tpa.use")) {
            return;
        }
        var pending = teleportService.pendingFor(player.getUniqueId());
        var result = teleportService.denyRequest(player);
        player.sendMessage(result.message());
        if (!result.success()) {
            return;
        }
        pending.ifPresent(request -> notifyRequester(player, request.from(), result, "denied"));
    }

    private static void notifyRequester(Player target, java.util.UUID requesterId, TeleportService.Result result, String action) {
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            return;
        }
        if (result.success()) {
            requester.sendMessage(target.getName() + " " + action + " your teleport request.");
            return;
        }
        requester.sendMessage("Teleport request to " + target.getName() + " failed: " + result.message());
    }
}
