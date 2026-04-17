package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.plugin.SchedulerBridge;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuService {
    private final CoreMenuService coreMenus;
    private final TopLevelMenuViews topLevelViews;
    private final TopLevelMenuActions topLevelActions;
    private final SchedulerBridge schedulerBridge;
    private final long autoRefreshTicks;
    private final Set<UUID> refreshScheduled = ConcurrentHashMap.newKeySet();

    public MenuService(
            PointsService pointsService,
            ProgressionService progressionService,
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            SchedulerBridge schedulerBridge,
            int autoRefreshSeconds,
            boolean showManualRefreshControls) {
        this.schedulerBridge = schedulerBridge;
        this.autoRefreshTicks = Math.max(1L, autoRefreshSeconds) * 20L;
        this.topLevelViews = new TopLevelMenuViews(pointsService, progressionService);
        CoreMenuViews coreViews = new CoreMenuViews(
                homeService,
                warpService,
                partyService,
                teleportService,
                showManualRefreshControls);
        this.coreMenus = new CoreMenuService(coreViews, this::openRoot);
        this.topLevelActions = new TopLevelMenuActions(pointsService, progressionService, topLevelViews, coreMenus);
    }

    public void openRoot(Player player) {
        topLevelViews.openRoot(player);
        ensureAutoRefresh(player);
    }

    public void openShop(Player player) {
        topLevelActions.resetShopSelection(player.getUniqueId());
        topLevelActions.setShopPage(player.getUniqueId(), 0);
        topLevelViews.openShop(player, topLevelActions.shopPage(player.getUniqueId()));
        ensureAutoRefresh(player);
    }

    public void openProgress(Player player) {
        topLevelActions.setProgressPage(player.getUniqueId(), 0);
        topLevelViews.openProgress(player, topLevelActions.progressPage(player.getUniqueId()));
        ensureAutoRefresh(player);
    }

    public void openTpAcceptPicker(Player player) throws Exception {
        coreMenus.open(player, MenuTitles.PICK_TP_ACCEPT);
        ensureAutoRefresh(player);
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
                ensureAutoRefresh(player);
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
                ensureAutoRefresh(player);
                return;
            }
            if (topLevelActions.handleClick(event, player, title, display)) {
                ensureAutoRefresh(player);
                return;
            }
            if (coreMenus.handleClick(event, player, title, display)) {
                ensureAutoRefresh(player);
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
            refreshScheduled.remove(player.getUniqueId());
            topLevelActions.clearPlayerState(player.getUniqueId());
            coreMenus.clearPlayerState(player.getUniqueId());
        });
    }

    private static String displayName(InventoryClickEvent event) {
        return event.getCurrentItem().getItemMeta() == null
                ? ""
                : event.getCurrentItem().getItemMeta().getDisplayName();
    }

    private void refreshOpenMenu(Player player) throws Exception {
        String title = player.getOpenInventory().getTitle();
        if (!MenuTitles.isPluginMenu(title)) {
            return;
        }
        switch (title) {
            case MenuTitles.ROOT -> topLevelViews.openRoot(player);
            case MenuTitles.SHOP -> topLevelViews.openShop(player, topLevelActions.shopPage(player.getUniqueId()));
            case MenuTitles.SHOP_DETAIL -> topLevelViews.openShopDetail(player, topLevelActions.shopSelection(player.getUniqueId()));
            case MenuTitles.PROGRESS -> topLevelViews.openProgress(player, topLevelActions.progressPage(player.getUniqueId()));
            default -> coreMenus.open(player, title);
        }
    }

    private void ensureAutoRefresh(Player player) {
        if (!requiresFallbackRefresh(player.getOpenInventory().getTitle())) {
            refreshScheduled.remove(player.getUniqueId());
            return;
        }
        UUID playerId = player.getUniqueId();
        if (!refreshScheduled.add(playerId)) {
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, autoRefreshTicks, () -> {
            refreshScheduled.remove(playerId);
            String openTitle = player.getOpenInventory().getTitle();
            if (!player.isOnline() || !MenuTitles.isPluginMenu(openTitle) || !requiresFallbackRefresh(openTitle)) {
                return;
            }
            try {
                refreshOpenMenu(player);
            } catch (Exception ex) {
                player.sendMessage("Menu refresh failed: " + ex.getMessage());
            }
            ensureAutoRefresh(player);
        });
    }

    private static boolean requiresFallbackRefresh(String title) {
        return MenuTitles.TELEPORT.equals(title)
                || MenuTitles.PICK_TPA.equals(title)
                || MenuTitles.PICK_TPA_HERE.equals(title)
                || MenuTitles.PICK_TP.equals(title)
                || MenuTitles.PICK_TP_ACCEPT.equals(title)
                || MenuTitles.PICK_INVITE.equals(title);
    }
}
