package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.gui.MenuService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class PointsCommand implements CommandExecutor {
    private final PointsService pointsService;
    private final MenuService menuService;
    private final AchievementService achievementService;
    private final ActionBarRouter actionBarHudService;

    public PointsCommand(
            PointsService pointsService,
            MenuService menuService,
            AchievementService achievementService,
            ActionBarRouter actionBarHudService) {
        this.pointsService = pointsService;
        this.menuService = menuService;
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandUtil.requirePlayer(sender).map(player -> {
            try {
                switch (command.getName().toLowerCase()) {
                    case "points" -> player.sendMessage("Maruishi Points: " + pointsService.getBalance(player.getUniqueId()));
                    case "convert" -> handleConvert(player, args);
                    case "shop" -> handleShop(player, args);
                    default -> {
                        return false;
                    }
                }
            } catch (Exception ex) {
                player.sendMessage("Operation failed: " + ex.getMessage());
            }
            return true;
        }).orElse(true);
    }

    private void handleConvert(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length < 2 || !args[0].equalsIgnoreCase("cobblestone")) {
            player.sendMessage("Usage: /convert cobblestone <amount>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage("Amount must be a number.");
            return;
        }
        var result = pointsService.convertCobblestone(player, amount);
        if (result.success() && result.amount() > 0) {
            achievementService.increment(player.getUniqueId(), "convert_amount", result.amount());
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
    }

    private void handleShop(org.bukkit.entity.Player player, String[] args) throws Exception {
        if (args.length == 0) {
            menuService.openShop(player);
            return;
        }
        if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("buy")) {
            int quantity = 1;
            if (args.length == 3) {
                try {
                    quantity = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    player.sendMessage("Quantity must be a number.");
                    return;
                }
            }
            var result = pointsService.purchase(player, args[1], quantity);
            if (result.success()) {
                achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", quantity);
                actionBarHudService.refreshIdle(player);
            }
            player.sendMessage(result.message());
            return;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("override")) {
            if (!CommandUtil.requirePermission(player, "lkjmcsmp.economy.override")) {
                return;
            }
            int points;
            try {
                points = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                player.sendMessage("Points must be a number.");
                return;
            }
            var result = pointsService.applyOverride(player, args[1], points);
            player.sendMessage(result.message());
            return;
        }
        player.sendMessage("Usage: /shop [buy <item> [quantity]|override <item> <points>]");
    }
}
