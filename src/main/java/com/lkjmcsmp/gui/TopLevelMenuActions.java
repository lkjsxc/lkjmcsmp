package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarHudService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

final class TopLevelMenuActions {
    private final PointsService pointsService;
    private final AchievementService achievementService;
    private final ActionBarHudService actionBarHudService;
    private final TopLevelMenuViews views;
    private final CoreMenuService coreMenus;
    private final TopLevelMenuState state = new TopLevelMenuState();

    TopLevelMenuActions(
            PointsService pointsService,
            AchievementService achievementService,
            ActionBarHudService actionBarHudService,
            TopLevelMenuViews views,
            CoreMenuService coreMenus) {
        this.pointsService = pointsService;
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
        this.views = views;
        this.coreMenus = coreMenus;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.ROOT -> handleRoot(player, display);
            case MenuTitles.SHOP -> handleShop(player, display);
            case MenuTitles.SHOP_DETAIL -> handleShopDetail(player, display);
            case MenuTitles.ACHIEVEMENT -> handleAchievement(event, player, display);
            default -> false;
        };
    }

    private boolean handleRoot(Player player, String display) throws Exception {
        if (display.equals("Points Shop")) {
            resetShopSelection(player.getUniqueId());
            setShopPage(player.getUniqueId(), 0);
            views.openShop(player, shopPage(player.getUniqueId()));
            return true;
        }
        if (display.equals("Achievement")) {
            setAchievementPage(player.getUniqueId(), 0);
            views.openAchievement(player, achievementPage(player.getUniqueId()));
            return true;
        }
        if (display.equals("Close Menu")) {
            player.closeInventory();
            return true;
        }
        if (!coreMenus.openFromRoot(player, display)) {
            player.sendMessage("Unknown menu action.");
        }
        return true;
    }

    private boolean handleShop(Player player, String display) throws Exception {
        UUID playerId = player.getUniqueId();
        if (display.equals("Convert Cobblestone")) {
            convertAllCobblestone(player);
            return true;
        }
        if (display.equals("Page Prev")) {
            setShopPage(playerId, Math.max(0, shopPage(playerId) - 1));
            views.openShop(player, shopPage(playerId));
            setShopPage(playerId, MenuPageStateSync.readCurrentPage(player, shopPage(playerId)));
            return true;
        }
        if (display.equals("Page Next")) {
            setShopPage(playerId, shopPage(playerId) + 1);
            views.openShop(player, shopPage(playerId));
            setShopPage(playerId, MenuPageStateSync.readCurrentPage(player, shopPage(playerId)));
            return true;
        }
        if (!display.startsWith("Item :: ")) {
            return false;
        }
        String key = display.substring("Item :: ".length()).trim().toLowerCase();
        if (!pointsService.getShopItems().containsKey(key)) {
            player.sendMessage("Unknown shop item.");
            return true;
        }
        state.setShopSelection(playerId, new ShopSelection(key));
        views.openShopDetail(player, state.shopSelection(playerId));
        return true;
    }

    private boolean handleShopDetail(Player player, String display) throws Exception {
        if (!display.startsWith("Buy x")) {
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(display.substring("Buy x".length()));
        } catch (NumberFormatException ex) {
            return false;
        }
        buySelected(player, quantity);
        return true;
    }

    private boolean handleAchievement(InventoryClickEvent event, Player player, String display) throws Exception {
        UUID playerId = player.getUniqueId();
        if (display.equals("Page Prev")) {
            setAchievementPage(playerId, Math.max(0, achievementPage(playerId) - 1));
            views.openAchievement(player, achievementPage(playerId));
            setAchievementPage(playerId, MenuPageStateSync.readCurrentPage(player, achievementPage(playerId)));
            return true;
        }
        if (display.equals("Page Next")) {
            setAchievementPage(playerId, achievementPage(playerId) + 1);
            views.openAchievement(player, achievementPage(playerId));
            setAchievementPage(playerId, MenuPageStateSync.readCurrentPage(player, achievementPage(playerId)));
            return true;
        }
        String achievementKey = AchievementMenuSupport.extractKey(event.getCurrentItem());
        if (achievementKey == null) {
            return true;
        }
        var claimResult = achievementService.claim(player.getUniqueId(), achievementKey);
        player.sendMessage(claimResult.message());
        if (claimResult.success()) {
            actionBarHudService.refreshIdle(player);
        }
        views.openAchievement(player, achievementPage(playerId));
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
        views.openShop(player, shopPage(player.getUniqueId()));
        setShopPage(player.getUniqueId(), MenuPageStateSync.readCurrentPage(player, shopPage(player.getUniqueId())));
    }

    void resetShopSelection(UUID playerId) { state.resetShopSelection(playerId); }
    void clearPlayerState(UUID playerId) { state.clear(playerId); }
    ShopSelection shopSelection(UUID playerId) { return state.shopSelection(playerId); }
    int shopPage(UUID playerId) { return state.shopPage(playerId); }
    int achievementPage(UUID playerId) { return state.achievementPage(playerId); }
    void setShopPage(UUID playerId, int page) { state.setShopPage(playerId, page); }
    void setAchievementPage(UUID playerId, int page) { state.setAchievementPage(playerId, page); }

    private void buySelected(Player player, int quantity) throws Exception {
        ShopSelection selection = state.shopSelection(player.getUniqueId());
        if (selection == null) {
            player.sendMessage("Select a shop item first.");
            views.openShop(player, shopPage(player.getUniqueId()));
            return;
        }
        var result = pointsService.purchase(player, selection.itemKey(), quantity);
        if (result.success()) {
            achievementService.increment(player.getUniqueId(), "shop_purchase_quantity", quantity);
            actionBarHudService.refreshIdle(player);
        }
        player.sendMessage(result.message());
        views.openShopDetail(player, state.shopSelection(player.getUniqueId()));
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
