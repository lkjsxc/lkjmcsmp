package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

final class TopLevelMenuActions {
    private final AchievementService achievementService;
    private final ActionBarRouter actionBarHudService;
    private final TopLevelMenuViews views;
    private final CoreMenuService coreMenus;
    private final ShopActions shopActions;
    private final TopLevelMenuState state = new TopLevelMenuState();

    TopLevelMenuActions(
            PointsService pointsService,
            AchievementService achievementService,
            ActionBarRouter actionBarHudService,
            TopLevelMenuViews views,
            CoreMenuService coreMenus) {
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
        this.views = views;
        this.coreMenus = coreMenus;
        this.shopActions = new ShopActions(
                pointsService, achievementService, actionBarHudService, views, state);
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.ROOT -> handleRoot(player, display);
            case MenuTitles.SHOP -> shopActions.handleShop(player, display);
            case MenuTitles.SHOP_DETAIL -> shopActions.handleShopDetail(player, display);
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

    void resetShopSelection(UUID playerId) { state.resetShopSelection(playerId); }
    void clearPlayerState(UUID playerId) { state.clear(playerId); }
    ShopSelection shopSelection(UUID playerId) { return state.shopSelection(playerId); }
    int shopPage(UUID playerId) { return state.shopPage(playerId); }
    int achievementPage(UUID playerId) { return state.achievementPage(playerId); }
    void setShopPage(UUID playerId, int page) { state.setShopPage(playerId, page); }
    void setAchievementPage(UUID playerId, int page) { state.setAchievementPage(playerId, page); }
}
