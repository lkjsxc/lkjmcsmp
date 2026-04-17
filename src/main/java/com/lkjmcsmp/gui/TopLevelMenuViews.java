package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.Map;

final class TopLevelMenuViews {
    private final PointsService pointsService;
    private final ProgressionService progressionService;

    TopLevelMenuViews(PointsService pointsService, ProgressionService progressionService) {
        this.pointsService = pointsService;
        this.progressionService = progressionService;
    }

    void openRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ROOT);
        inventory.setItem(19, MenuItems.named(Material.ENDER_PEARL, "Teleport"));
        inventory.setItem(20, MenuItems.named(Material.RED_BED, "Homes"));
        inventory.setItem(21, MenuItems.named(Material.COMPASS, "Warps"));
        inventory.setItem(22, MenuItems.named(Material.PLAYER_HEAD, "Team"));
        inventory.setItem(23, MenuItems.named(Material.COBBLESTONE, "Points Shop"));
        inventory.setItem(24, MenuItems.named(Material.BOOK, "Progression"));
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(Material.BARRIER, "Close Menu"));
        player.openInventory(inventory);
    }

    void openShop(Player player) {
        openShop(player, null);
    }

    void openShop(Player player, ShopSelection selection) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP);
        int slot = 0;
        Map<String, com.lkjmcsmp.domain.model.ShopEntry> items = pointsService.getShopItems();
        for (Map.Entry<String, com.lkjmcsmp.domain.model.ShopEntry> entry : items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .toList()) {
            if (slot >= MenuLayout.CONTENT_LIMIT) {
                break;
            }
            var value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Item :: " + entry.getKey());
            meta.setLore(java.util.List.of(
                    "Unit bundle: " + value.quantity() + "x " + value.material(),
                    "Unit cost: " + value.points() + " points"));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
        ShopSelection active = resolveSelection(selection, items);
        renderShopControls(inventory, active, items);
        inventory.setItem(MenuLayout.SHOP_CONVERT_SLOT, MenuItems.named(
                Material.COBBLESTONE,
                "Convert Cobblestone",
                "Converts all cobblestone in inventory to points"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    void openProgress(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.PROGRESS);
        try {
            int slot = 0;
            for (var view : progressionService.getViews(player.getUniqueId()).values()) {
                if (slot >= MenuLayout.CONTENT_LIMIT) {
                    break;
                }
                inventory.setItem(slot++, ProgressMenuSupport.toItem(view));
            }
        } catch (Exception ex) {
            player.sendMessage("Failed to load progression: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    private static ShopSelection resolveSelection(
            ShopSelection selection,
            Map<String, com.lkjmcsmp.domain.model.ShopEntry> items) {
        if (selection == null || !items.containsKey(selection.itemKey())) {
            return null;
        }
        return selection;
    }

    private static void renderShopControls(
            Inventory inventory,
            ShopSelection selection,
            Map<String, com.lkjmcsmp.domain.model.ShopEntry> items) {
        inventory.setItem(46, MenuItems.named(Material.RED_DYE, "Quantity -1", "Decrease selected units"));
        inventory.setItem(47, MenuItems.named(Material.LIME_DYE, "Quantity +1", "Increase selected units"));
        if (selection == null) {
            inventory.setItem(48, MenuItems.named(Material.GRAY_DYE, "Buy Selected", "Select an item first"));
            inventory.setItem(50, MenuItems.named(Material.PAPER, "No Item Selected", "Click an item above"));
            return;
        }
        var selected = items.get(selection.itemKey());
        int totalPoints = selected.points() * selection.units();
        int totalQuantity = selected.quantity() * selection.units();
        inventory.setItem(48, MenuItems.named(
                Material.GOLD_INGOT,
                "Buy Selected",
                "Unit: " + selected.points() + " pts",
                "Units: " + selection.units(),
                "Total: " + totalPoints + " pts"));
        inventory.setItem(50, MenuItems.named(
                selected.material(),
                "Selected :: " + selection.itemKey(),
                "Receive: " + totalQuantity + "x " + selected.material(),
                "Total cost: " + totalPoints + " points"));
    }
}
