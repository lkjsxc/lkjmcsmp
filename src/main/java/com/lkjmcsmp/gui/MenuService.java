package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.plugin.hud.ActionBarHudService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class MenuService {
    private final CoreMenuService coreMenus;
    private final TopLevelMenuViews topLevelViews;
    private final TopLevelMenuActions topLevelActions;
    private final SchedulerBridge schedulerBridge;

    public MenuService(
            PointsService pointsService,
            AchievementService achievementService,
            ActionBarHudService actionBarHudService,
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            SchedulerBridge schedulerBridge) {
        this.schedulerBridge = schedulerBridge;
        this.topLevelViews = new TopLevelMenuViews(pointsService, achievementService);
        CoreMenuViews coreViews = new CoreMenuViews(
                homeService,
                warpService,
                partyService,
                teleportService);
        this.coreMenus = new CoreMenuService(coreViews, this::openRoot);
        this.topLevelActions = new TopLevelMenuActions(
                pointsService,
                achievementService,
                actionBarHudService,
                topLevelViews,
                coreMenus);
    }

    public void openRoot(Player player) {
        topLevelViews.openRoot(player);
    }

    public void openShop(Player player) {
        topLevelActions.resetShopSelection(player.getUniqueId());
        topLevelActions.setShopPage(player.getUniqueId(), 0);
        topLevelViews.openShop(player, topLevelActions.shopPage(player.getUniqueId()));
    }

    public void openAchievement(Player player) {
        topLevelActions.setAchievementPage(player.getUniqueId(), 0);
        topLevelViews.openAchievement(player, topLevelActions.achievementPage(player.getUniqueId()));
    }

    public void openTpAcceptPicker(Player player) throws Exception {
        coreMenus.open(player, MenuTitles.PICK_TP_ACCEPT);
    }

    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        String title = event.getView().getTitle();
        if (!MenuTitles.isPluginMenu(title)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String display = displayName(event);
        if (display.startsWith("Page ::")) {
            return;
        }
        try {
            if (MenuTitles.ROOT.equals(title) && topLevelActions.handleClick(event, player, title, display)) {
                return;
            }
            if (display.equals("Back")) {
                if (!coreMenus.openBack(player, title)) {
                    if (MenuTitles.SHOP_DETAIL.equals(title)) {
                        topLevelViews.openShop(player, topLevelActions.shopPage(player.getUniqueId()));
                    } else {
                        openRoot(player);
                    }
                }
                return;
            }
            if (topLevelActions.handleClick(event, player, title, display)) {
                return;
            }
            if (coreMenus.handleClick(event, player, title, display)) {
                return;
            }
            player.sendMessage("Unknown menu action.");
        } catch (Exception ex) {
            player.sendMessage("Menu action failed: " + ex.getMessage());
        }
    }

    public void onClose(InventoryCloseEvent event) {
        String closedTitle = event.getView().getTitle();
        if (!MenuTitles.isPluginMenu(closedTitle) || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, 1L, () -> {
            if (MenuTitles.isPluginMenu(player.getOpenInventory().getTitle())) {
                return;
            }
            topLevelActions.clearPlayerState(player.getUniqueId());
            coreMenus.clearPlayerState(player.getUniqueId());
        });
    }

    private static String displayName(InventoryClickEvent event) {
        return event.getCurrentItem().getItemMeta() == null
                ? ""
                : event.getCurrentItem().getItemMeta().getDisplayName();
    }
}
