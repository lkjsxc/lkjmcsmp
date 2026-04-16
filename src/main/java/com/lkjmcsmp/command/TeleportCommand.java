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
                    case "tpaccept" -> teleportService.acceptRequest(player, result -> player.sendMessage(result.message()));
                    case "tpdeny" -> player.sendMessage(teleportService.denyRequest(player).message());
                    case "rtp" -> {
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
        if (args.length == 0) {
            player.sendMessage("Usage: " + (summonHere ? "/tpahere <player>" : "/tpa <player>"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("Target offline.");
            return;
        }
        player.sendMessage(teleportService.requestTeleport(player, target, summonHere).message());
    }
}
