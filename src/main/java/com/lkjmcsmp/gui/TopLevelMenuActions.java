package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class TopLevelMenuActions {
    private final PointsService pointsService;
    private final ProgressionService progressionService;
    private final TopLevelMenuViews views;
    private final CoreMenuService coreMenus;
    private final Map<UUID, ShopSelection> shopSelections = new ConcurrentHashMap<>();

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
            case MenuTitles.PROGRESS -> handleProgress(event, player);
            default -> false;
        };
    }

    private boolean handleRoot(Player player, String display) throws Exception {
        if (display.equals("Points Shop")) {
            resetShopSelection(player.getUniqueId());
            views.openShop(player, shopSelections.get(player.getUniqueId()));
            return true;
        }
        if (display.equals("Progression")) {
            views.openProgress(player);
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
        if (display.equals("Convert Cobblestone")) {
            convertAllCobblestone(player);
            return true;
        }
        if (display.equals("Quantity -1")) {
            adjustSelection(player.getUniqueId(), -1);
            views.openShop(player, shopSelections.get(player.getUniqueId()));
            return true;
        }
        if (display.equals("Quantity +1")) {
            adjustSelection(player.getUniqueId(), 1);
            views.openShop(player, shopSelections.get(player.getUniqueId()));
            return true;
        }
        if (display.equals("Buy Selected")) {
            buySelected(player);
            return true;
        }
        if (display.startsWith("Item :: ")) {
            String key = display.substring("Item :: ".length()).trim().toLowerCase();
            if (!pointsService.getShopItems().containsKey(key)) {
                player.sendMessage("Unknown shop item.");
                return true;
            }
            shopSelections.put(player.getUniqueId(), new ShopSelection(key, 1));
            views.openShop(player, shopSelections.get(player.getUniqueId()));
            return true;
        }
        return false;
    }

    private boolean handleProgress(InventoryClickEvent event, Player player) throws Exception {
        String milestoneKey = ProgressMenuSupport.extractKey(event.getCurrentItem());
        if (milestoneKey == null) {
            return true;
        }
        player.sendMessage(progressionService.claim(player.getUniqueId(), milestoneKey).message());
        views.openProgress(player);
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
        views.openShop(player, shopSelections.get(player.getUniqueId()));
    }

    void resetShopSelection(UUID playerId) {
        shopSelections.remove(playerId);
    }

    void clearPlayerState(UUID playerId) {
        shopSelections.remove(playerId);
    }

    ShopSelection shopSelection(UUID playerId) {
        return shopSelections.get(playerId);
    }

    private void adjustSelection(UUID playerId, int delta) {
        ShopSelection current = shopSelections.get(playerId);
        if (current == null) {
            return;
        }
        shopSelections.put(playerId, current.withDelta(delta));
    }

    private void buySelected(Player player) throws Exception {
        ShopSelection selection = shopSelections.get(player.getUniqueId());
        if (selection == null) {
            player.sendMessage("Select a shop item first.");
            views.openShop(player, null);
            return;
        }
        var result = pointsService.purchase(player, selection.itemKey(), selection.units());
        player.sendMessage(result.message());
        views.openShop(player, shopSelections.get(player.getUniqueId()));
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
