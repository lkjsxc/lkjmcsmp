package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

final class TopLevelMenuActions {
    private final PointsService pointsService;
    private final ProgressionService progressionService;
    private final TopLevelMenuViews views;
    private final CoreMenuService coreMenus;
    private final TopLevelMenuState state = new TopLevelMenuState();

    TopLevelMenuActions(
            PointsService pointsService,
            ProgressionService progressionService,
            TopLevelMenuViews views,
            CoreMenuService coreMenus) {
        this.pointsService = pointsService;
        this.progressionService = progressionService;
        this.views = views;
        this.coreMenus = coreMenus;
    }

    boolean handleClick(InventoryClickEvent event, Player player, String title, String display) throws Exception {
        return switch (title) {
            case MenuTitles.ROOT -> handleRoot(player, display);
            case MenuTitles.SHOP -> handleShop(player, display);
            case MenuTitles.SHOP_DETAIL -> handleShopDetail(player, display);
            case MenuTitles.PROGRESS -> handleProgress(event, player, display);
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
        if (display.equals("Progression")) {
            setProgressPage(player.getUniqueId(), 0);
            views.openProgress(player, progressPage(player.getUniqueId()));
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
        state.setShopSelection(playerId, new ShopSelection(key, 1));
        views.openShopDetail(player, state.shopSelection(playerId));
        return true;
    }

    private boolean handleShopDetail(Player player, String display) throws Exception {
        UUID playerId = player.getUniqueId();
        return switch (display) {
            case "Set Quantity 1" -> reopenAfterSet(player, playerId, 1);
            case "Set Quantity 64" -> reopenAfterSet(player, playerId, 64);
            case "Quantity -8" -> reopenAfterDelta(player, playerId, -8);
            case "Quantity -1" -> reopenAfterDelta(player, playerId, -1);
            case "Quantity +1" -> reopenAfterDelta(player, playerId, 1);
            case "Quantity +8" -> reopenAfterDelta(player, playerId, 8);
            case "Buy Selected" -> {
                buySelected(player);
                yield true;
            }
            default -> false;
        };
    }

    private boolean reopenAfterDelta(Player player, UUID playerId, int delta) {
        state.adjustSelection(playerId, delta);
        views.openShopDetail(player, state.shopSelection(playerId));
        return true;
    }

    private boolean reopenAfterSet(Player player, UUID playerId, int quantity) {
        ShopSelection current = state.shopSelection(playerId);
        if (current != null) {
            state.setShopSelection(playerId, current.withQuantity(quantity));
        }
        views.openShopDetail(player, state.shopSelection(playerId));
        return true;
    }

    private boolean handleProgress(InventoryClickEvent event, Player player, String display) throws Exception {
        UUID playerId = player.getUniqueId();
        if (display.equals("Page Prev")) {
            setProgressPage(playerId, Math.max(0, progressPage(playerId) - 1));
            views.openProgress(player, progressPage(playerId));
            setProgressPage(playerId, MenuPageStateSync.readCurrentPage(player, progressPage(playerId)));
            return true;
        }
        if (display.equals("Page Next")) {
            setProgressPage(playerId, progressPage(playerId) + 1);
            views.openProgress(player, progressPage(playerId));
            setProgressPage(playerId, MenuPageStateSync.readCurrentPage(player, progressPage(playerId)));
            return true;
        }
        String milestoneKey = ProgressMenuSupport.extractKey(event.getCurrentItem());
        if (milestoneKey == null) {
            return true;
        }
        player.sendMessage(progressionService.claim(player.getUniqueId(), milestoneKey).message());
        views.openProgress(player, progressPage(playerId));
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
            progressionService.increment(player.getUniqueId(), "convert_amount", result.amount());
        }
        player.sendMessage(result.message());
        views.openShop(player, shopPage(player.getUniqueId()));
        setShopPage(player.getUniqueId(), MenuPageStateSync.readCurrentPage(player, shopPage(player.getUniqueId())));
    }

    void resetShopSelection(UUID playerId) { state.resetShopSelection(playerId); }
    void clearPlayerState(UUID playerId) { state.clear(playerId); }
    ShopSelection shopSelection(UUID playerId) { return state.shopSelection(playerId); }
    int shopPage(UUID playerId) { return state.shopPage(playerId); }
    int progressPage(UUID playerId) { return state.progressPage(playerId); }
    void setShopPage(UUID playerId, int page) { state.setShopPage(playerId, page); }
    void setProgressPage(UUID playerId, int page) { state.setProgressPage(playerId, page); }

    private void buySelected(Player player) throws Exception {
        ShopSelection selection = state.shopSelection(player.getUniqueId());
        if (selection == null) {
            player.sendMessage("Select a shop item first.");
            views.openShop(player, shopPage(player.getUniqueId()));
            return;
        }
        var result = pointsService.purchase(player, selection.itemKey(), selection.quantity());
        if (result.success()) {
            progressionService.increment(player.getUniqueId(), "shop_purchase_quantity", selection.quantity());
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
