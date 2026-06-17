package com.lkjmcsmp.command;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.ShopEffectExecutor;
import com.lkjmcsmp.domain.model.ShopEntry;
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
                    case "points" -> player.sendMessage("Cobblestone Points: " + pointsService.getBalance(player.getUniqueId()));
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
            String itemKey = args[1].toLowerCase();
            ShopEntry entry = pointsService.getShopItems().get(itemKey);
            var result = entry != null && entry.service()
                    ? pointsService.purchase(player, itemKey, quantity,
                    finalResult -> finishServicePurchase(player, entry, finalResult))
                    : pointsService.purchase(player, itemKey, quantity);
            if (result.pending()) {
                player.sendMessage(result.message());
                return;
            }
            if (result.success()) {
                achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", quantity);
                if (entry != null) {
                    actionBarHudService.onShopPurchase(player, itemKey, entry.points() * quantity);
                }
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

    private void finishServicePurchase(
            org.bukkit.entity.Player player,
            ShopEntry entry,
            ShopEffectExecutor.Result result) {
        if (result.success()) {
            try {
                achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", 1);
            } catch (Exception ignored) {
            }
            actionBarHudService.onShopPurchase(player, entry.key(), entry.points());
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
    }
}
