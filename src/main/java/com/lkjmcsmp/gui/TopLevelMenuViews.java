package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.List;
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

    void openShop(Player player, int page) {
        Map<String, ShopEntry> items = pointsService.getShopItems();
        List<Map.Entry<String, ShopEntry>> sorted = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .toList();
        int bounded = MenuPagination.clampPage(page, sorted.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP);
        int slot = 0;
        for (Map.Entry<String, ShopEntry> entry : MenuPagination.pageSlice(sorted, bounded)) {
            ShopEntry value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Item :: " + entry.getKey());
            meta.setLore(java.util.List.of(
                    "Price: " + value.points() + " points per item",
                    "Selectable quantity: 1..64",
                    "Click to open purchase details"));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
        if (sorted.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Shop Items"));
        }
        inventory.setItem(MenuLayout.SHOP_CONVERT_SLOT, MenuItems.named(
                Material.COBBLESTONE,
                "Convert Cobblestone",
                "Converts all cobblestone in inventory to points"));
        MenuPagination.renderControls(inventory, bounded, sorted.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    void openShopDetail(Player player, ShopSelection selection) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP_DETAIL);
        if (selection == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Item Selected", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
            player.openInventory(inventory);
            return;
        }
        ShopEntry selected = pointsService.getShopItems().get(selection.itemKey());
        if (selected == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "Unknown Item", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
            player.openInventory(inventory);
            return;
        }
        int totalPoints = selected.points() * selection.quantity();
        int totalQuantity = selection.quantity();
        inventory.setItem(13, MenuItems.named(
                selected.material(),
                "Selected :: " + selected.key(),
                "Receive: " + totalQuantity + "x " + selected.material(),
                "Price: " + selected.points() + " points each"));
        inventory.setItem(20, MenuItems.named(Material.CLOCK, "Set Quantity 1"));
        inventory.setItem(21, MenuItems.named(Material.RED_DYE, "Quantity -8"));
        inventory.setItem(22, MenuItems.named(Material.REDSTONE, "Quantity -1"));
        inventory.setItem(23, MenuItems.named(
                Material.PAPER,
                "Quantity :: " + selection.quantity(),
                "Total cost: " + totalPoints + " points"));
        inventory.setItem(24, MenuItems.named(Material.LIME_DYE, "Quantity +1"));
        inventory.setItem(25, MenuItems.named(Material.EMERALD, "Quantity +8"));
        inventory.setItem(26, MenuItems.named(Material.CLOCK, "Set Quantity 64"));
        inventory.setItem(31, MenuItems.named(
                Material.GOLD_INGOT,
                "Buy Selected",
                "Quantity: " + selection.quantity(),
                "Total: " + totalPoints + " points"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    void openProgress(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.PROGRESS);
        try {
            List<ProgressionService.MilestoneView> views = progressionService.getViews(player.getUniqueId())
                    .values()
                    .stream()
                    .toList();
            int bounded = MenuPagination.clampPage(page, views.size());
            int slot = 0;
            for (var view : MenuPagination.pageSlice(views, bounded)) {
                inventory.setItem(slot++, ProgressMenuSupport.toItem(view));
            }
            if (views.isEmpty()) {
                inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Milestones"));
            }
            MenuPagination.renderControls(inventory, bounded, views.size());
        } catch (Exception ex) {
            player.sendMessage("Failed to load progression: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }
}
