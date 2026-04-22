package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.achievement.AchievementService;
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
    private final AchievementService achievementService;

    TopLevelMenuViews(PointsService pointsService, AchievementService achievementService) {
        this.pointsService = pointsService;
        this.achievementService = achievementService;
    }

    void openRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ROOT);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("lkjmcsmp Menu"));
        inventory.setItem(10, MenuItems.named(Material.ENDER_PEARL, "Teleport"));
        inventory.setItem(12, MenuItems.named(Material.RED_BED, "Homes"));
        inventory.setItem(14, MenuItems.named(Material.COMPASS, "Warps"));
        inventory.setItem(16, MenuItems.named(Material.PLAYER_HEAD, "Team"));
        inventory.setItem(20, MenuItems.named(Material.COBBLESTONE, "Points Shop"));
        inventory.setItem(22, MenuItems.named(Material.BOOK, "Achievement"));
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(Material.BARRIER, "Close Menu"));
        MenuDecor.fillBorder(inventory, MenuDecor.ROOT_BORDER);
        player.openInventory(inventory);
    }

    void openShop(Player player, int page) {
        Map<String, ShopEntry> items = pointsService.getShopItems();
        List<Map.Entry<String, ShopEntry>> sorted = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .toList();
        int bounded = MenuPagination.clampPage(page, sorted.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP);
        int points = safePoints(player);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Points Shop", "Your balance: " + points + " points"));
        int slotIdx = 0;
        for (Map.Entry<String, ShopEntry> entry : MenuPagination.pageSlice(sorted, bounded)) {
            ShopEntry value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Item :: " + entry.getKey());
            meta.setLore(List.of(
                    "Price: " + value.points() + " points",
                    value.service() ? "\u00A7dService Item — executes on purchase" : "Selectable quantity: 1..64",
                    "Click to open purchase details"));
            item.setItemMeta(meta);
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], item);
            }
            slotIdx++;
        }
        if (sorted.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Shop Items"));
        }
        inventory.setItem(MenuLayout.SHOP_CONVERT_SLOT, MenuItems.named(
                Material.COBBLESTONE,
                "Convert Cobblestone",
                "Converts all cobblestone in inventory to points"));
        inventory.setItem(50, playerPointsItem(player));
        MenuPagination.renderControls(inventory, bounded, sorted.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
        player.openInventory(inventory);
    }

    void openShopDetail(Player player, ShopSelection selection) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP_DETAIL);
        if (selection == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Item Selected", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
            MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
            player.openInventory(inventory);
            return;
        }
        ShopEntry selected = pointsService.getShopItems().get(selection.itemKey());
        if (selected == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "Unknown Item", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
            MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
            player.openInventory(inventory);
            return;
        }
        int points = safePoints(player);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Buying: " + selected.key(), "Price: " + selected.points() + " points"));
        inventory.setItem(13, MenuItems.named(
                selected.material(),
                "Selected :: " + selected.key(),
                "Price: " + selected.points() + " points",
                selected.service() ? "\u00A7dService — executes on purchase" : "Direct buy amounts: 1, 2, 4, 8, 16, 32, 64",
                "Click a button to purchase immediately."));
        inventory.setItem(31, MenuItems.named(
                Material.SUNFLOWER,
                "Your Points",
                "Balance: " + points,
                "Price: " + selected.points() + " points"));
        if (selected.service()) {
            int total = selected.points();
            boolean affordable = points >= total;
            inventory.setItem(22, MenuItems.named(
                    affordable ? Material.LIME_DYE : Material.BARRIER,
                    affordable ? "Purchase" : "Purchase (Locked)",
                    "Cost: " + total + " points",
                    affordable ? "Click to purchase now" : "Not enough points"));
        } else {
            inventory.setItem(19, quantityItem(selected.points(), points, 1));
            inventory.setItem(20, quantityItem(selected.points(), points, 2));
            inventory.setItem(21, quantityItem(selected.points(), points, 4));
            inventory.setItem(22, quantityItem(selected.points(), points, 8));
            inventory.setItem(23, quantityItem(selected.points(), points, 16));
            inventory.setItem(24, quantityItem(selected.points(), points, 32));
            inventory.setItem(25, quantityItem(selected.points(), points, 64));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
        player.openInventory(inventory);
    }

    void openAchievement(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ACHIEVEMENT);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Achievements"));
        try {
            List<AchievementService.AchievementView> views = achievementService.getViews(player.getUniqueId())
                    .values()
                    .stream()
                    .toList();
            int bounded = MenuPagination.clampPage(page, views.size());
            int slotIdx = 0;
            for (var view : MenuPagination.pageSlice(views, bounded)) {
                if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                    inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], AchievementMenuSupport.toItem(view));
                }
                slotIdx++;
            }
            if (views.isEmpty()) {
                inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Achievements"));
            }
            MenuPagination.renderControls(inventory, bounded, views.size());
        } catch (Exception ex) {
            player.sendMessage("Failed to load achievement: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.ACHIEVEMENT_BORDER);
        player.openInventory(inventory);
    }

    private ItemStack playerPointsItem(Player player) {
        return MenuItems.named(Material.SUNFLOWER, "Your Points", "Balance: " + safePoints(player));
    }

    private static ItemStack quantityItem(int pointsPerItem, int balance, int quantity) {
        int total = pointsPerItem * quantity;
        boolean affordable = balance >= total;
        return MenuItems.named(
                affordable ? Material.LIME_DYE : Material.BARRIER,
                "Buy x" + quantity,
                "Cost: " + total + " points",
                affordable ? "Click to purchase now" : "Not enough points");
    }

    private int safePoints(Player player) {
        try {
            return pointsService.getBalance(player.getUniqueId());
        } catch (Exception ex) {
            return 0;
        }
    }
}
