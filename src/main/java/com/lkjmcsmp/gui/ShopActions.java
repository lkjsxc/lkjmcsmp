package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

final class ShopActions {
    private final PointsService pointsService;
    private final AchievementService achievementService;
    private final ActionBarRouter actionBarHudService;
    private final TopLevelMenuViews views;
    private final TopLevelMenuState state;

    ShopActions(
            PointsService pointsService,
            AchievementService achievementService,
            ActionBarRouter actionBarHudService,
            TopLevelMenuViews views,
            TopLevelMenuState state) {
        this.pointsService = pointsService;
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
        this.views = views;
        this.state = state;
    }

    boolean handleShop(Player player, String action, String payload) throws Exception {
        UUID playerId = player.getUniqueId();
        if (action.equals("shop.convert")) {
            convertAllCobblestone(player);
            return true;
        }
        if (action.equals("page.prev")) {
            state.setShopPage(playerId, Math.max(0, state.shopPage(playerId) - 1));
            views.openShop(player, state.shopPage(playerId));
            state.setShopPage(playerId, MenuPageStateSync.readCurrentPage(player, state.shopPage(playerId)));
            return true;
        }
        if (action.equals("page.next")) {
            state.setShopPage(playerId, state.shopPage(playerId) + 1);
            views.openShop(player, state.shopPage(playerId));
            state.setShopPage(playerId, MenuPageStateSync.readCurrentPage(player, state.shopPage(playerId)));
            return true;
        }
        if (!action.equals("shop.select")) {
            return false;
        }
        String key = payload.trim().toLowerCase();
        if (!pointsService.getShopItems().containsKey(key)) {
            player.sendMessage("Unknown shop item.");
            return true;
        }
        state.setShopSelection(playerId, new ShopSelection(key));
        views.openShopDetail(player, state.shopSelection(playerId));
        return true;
    }

    boolean handleShopDetail(Player player, String action, String payload) throws Exception {
        if (action.equals("shop.purchase.service")) {
            buySelected(player, 1);
            return true;
        }
        if (!action.equals("shop.purchase.quantity")) {
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(payload);
        } catch (NumberFormatException ex) {
            return false;
        }
        buySelected(player, quantity);
        return true;
    }

    private void convertAllCobblestone(Player player) throws Exception {
        int requested = countCobblestone(player);
        if (requested <= 0) {
            player.sendMessage("no cobblestone available");
            return;
        }
        var result = pointsService.convertCobblestone(player, requested);
        if (result.success() && result.amount() > 0) {
            achievementService.increment(player.getUniqueId(), "convert_amount", result.amount());
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
        views.openShop(player, state.shopPage(player.getUniqueId()));
        state.setShopPage(player.getUniqueId(), MenuPageStateSync.readCurrentPage(player, state.shopPage(player.getUniqueId())));
    }

    private void buySelected(Player player, int quantity) throws Exception {
        ShopSelection selection = state.shopSelection(player.getUniqueId());
        if (selection == null) {
            player.sendMessage("Select a shop item first.");
            views.openShop(player, state.shopPage(player.getUniqueId()));
            return;
        }
        ShopEntry entry = pointsService.getShopItems().get(selection.itemKey());
        boolean isService = entry != null && entry.service();
        int actualQuantity = isService ? 1 : quantity;
        var result = isService
                ? pointsService.purchase(player, selection.itemKey(), actualQuantity,
                finalResult -> finishServicePurchase(player, selection, entry, actualQuantity, finalResult))
                : pointsService.purchase(player, selection.itemKey(), actualQuantity);
        if (result.pending()) {
            player.sendMessage(result.message());
            return;
        }
        if (result.success()) {
            achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", actualQuantity);
            int cost = entry != null ? entry.points() * actualQuantity : 0;
            actionBarHudService.onShopPurchase(player, selection.itemKey(), cost);
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
        if (isService) {
            views.openShop(player, state.shopPage(player.getUniqueId()));
            state.setShopPage(player.getUniqueId(), MenuPageStateSync.readCurrentPage(player, state.shopPage(player.getUniqueId())));
        } else {
            views.openShopDetail(player, state.shopSelection(player.getUniqueId()));
        }
    }

    private void finishServicePurchase(Player player, ShopSelection selection, ShopEntry entry, int quantity,
                                       com.lkjmcsmp.domain.ShopEffectExecutor.Result result) {
        if (result.success()) {
            try {
                achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", quantity);
            } catch (Exception ignored) {
            }
            int cost = entry != null ? entry.points() * quantity : 0;
            actionBarHudService.onShopPurchase(player, selection.itemKey(), cost);
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
        views.openShop(player, state.shopPage(player.getUniqueId()));
        state.setShopPage(player.getUniqueId(), MenuPageStateSync.readCurrentPage(player, state.shopPage(player.getUniqueId())));
    }

    private static int countCobblestone(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.COBBLESTONE) {
                count += stack.getAmount();
            }
        }
        return count;
    }


}
