package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.domain.PlayerSettingsService;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarRouter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

final class TopLevelMenuActions {
    private final AchievementService achievementService;
    private final ActionBarRouter actionBarHudService;
    private final PlayerSettingsService settingsService;
    private final MessageService messages;
    private final TopLevelMenuViews views;
    private final CoreMenuService coreMenus;
    private final ShopActions shopActions;
    private final TopLevelMenuState state = new TopLevelMenuState();

    TopLevelMenuActions(
            PointsService pointsService,
            AchievementService achievementService,
            ActionBarRouter actionBarHudService,
            PlayerSettingsService settingsService,
            MessageService messages,
            TopLevelMenuViews views,
            CoreMenuService coreMenus) {
        this.achievementService = achievementService;
        this.actionBarHudService = actionBarHudService;
        this.settingsService = settingsService;
        this.messages = messages;
        this.views = views;
        this.coreMenus = coreMenus;
        this.shopActions = new ShopActions(
                pointsService, achievementService, actionBarHudService, views, state);
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        String action = MenuAction.action(event.getCurrentItem());
        if (!action.isBlank()) {
            return handleAction(player, title, action, MenuAction.payload(event.getCurrentItem()));
        }
        return switch (title) {
            case MenuTitles.ROOT, MenuTitles.SETTINGS, MenuTitles.LANGUAGE -> false;
            case MenuTitles.SHOP -> shopActions.handleShop(player, display);
            case MenuTitles.SHOP_DETAIL -> shopActions.handleShopDetail(player, display);
            case MenuTitles.ACHIEVEMENT -> handleAchievement(event, player, display);
            case MenuTitles.PROFILE -> handleProfile(player, display);
            default -> false;
        };
    }

    private boolean handleAction(Player player, String title, String action, String payload) throws Exception {
        return switch (action) {
            case "root.teleport" -> coreMenus.open(player, MenuTitles.TELEPORT);
            case "root.homes" -> coreMenus.open(player, MenuTitles.HOMES);
            case "root.warps" -> coreMenus.open(player, MenuTitles.WARPS);
            case "root.team" -> coreMenus.open(player, MenuTitles.TEAM);
            case "root.shop" -> openShopRoot(player);
            case "root.achievement" -> openAchievementRoot(player);
            case "root.profile" -> { views.openProfile(player); yield true; }
            case "root.settings" -> { views.openSettings(player); yield true; }
            case "root.close" -> { player.closeInventory(); yield true; }
            case "settings.language" -> { views.openLanguage(player); yield true; }
            case "settings.hotbar" -> toggleHotbar(player);
            case "language.set" -> setLanguage(player, payload);
            case "nav.back" -> backFromAction(player, title);
            default -> false;
        };
    }

    private boolean openShopRoot(Player player) {
        resetShopSelection(player.getUniqueId());
        setShopPage(player.getUniqueId(), 0);
        views.openShop(player, shopPage(player.getUniqueId()));
        return true;
    }

    private boolean openAchievementRoot(Player player) {
        setAchievementPage(player.getUniqueId(), 0);
        views.openAchievement(player, achievementPage(player.getUniqueId()));
        return true;
    }

    private boolean toggleHotbar(Player player) throws Exception {
        boolean enabled = settingsService.toggleHotbarMenu(player.getUniqueId()).hotbarMenuEnabled();
        player.sendMessage(messages.get(player, enabled ? "settings.hotbar.enabled" : "settings.hotbar.disabled"));
        views.openSettings(player);
        return true;
    }

    private boolean setLanguage(Player player, String language) throws Exception {
        settingsService.setLanguage(player.getUniqueId(), language);
        player.sendMessage(messages.get(player, language.equals("ja")
                ? "settings.language.changed.ja" : "settings.language.changed"));
        views.openLanguage(player);
        return true;
    }

    private boolean backFromAction(Player player, String title) throws Exception {
        if (MenuTitles.LANGUAGE.equals(title)) {
            views.openSettings(player);
            return true;
        }
        if (MenuTitles.SETTINGS.equals(title)) {
            views.openRoot(player);
            return true;
        }
        return false;
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

    private boolean handleProfile(Player player, String display) throws Exception {
        return switch (display) {
            case "Team" -> {
                coreMenus.open(player, MenuTitles.TEAM);
                yield true;
            }
            case "Achievements" -> {
                setAchievementPage(player.getUniqueId(), 0);
                views.openAchievement(player, achievementPage(player.getUniqueId()));
                yield true;
            }
            case "Back" -> {
                coreMenus.openBack(player, MenuTitles.PROFILE);
                yield true;
            }
            default -> true;
        };
    }

    void resetShopSelection(UUID playerId) { state.resetShopSelection(playerId); }
    void clearPlayerState(UUID playerId) { state.clear(playerId); }
    ShopSelection shopSelection(UUID playerId) { return state.shopSelection(playerId); }
    int shopPage(UUID playerId) { return state.shopPage(playerId); }
    int achievementPage(UUID playerId) { return state.achievementPage(playerId); }
    void setShopPage(UUID playerId, int page) { state.setShopPage(playerId, page); }
    void setAchievementPage(UUID playerId, int page) { state.setAchievementPage(playerId, page); }
}
