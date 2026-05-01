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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class TopLevelMenuViews {
    private final PointsService pointsService;
    private final AchievementService achievementService;
    private final ProfileMenuView profileMenuView;
    private final RootSettingsMenuView rootSettingsView;

    TopLevelMenuViews(
            PointsService pointsService,
            AchievementService achievementService,
            ProfileMenuView profileMenuView,
            RootSettingsMenuView rootSettingsView) {
        this.pointsService = pointsService;
        this.achievementService = achievementService;
        this.profileMenuView = profileMenuView;
        this.rootSettingsView = rootSettingsView;
    }

    void openRoot(Player player) {
        rootSettingsView.openRoot(player);
    }

    void openSettings(Player player) {
        rootSettingsView.openSettings(player);
    }

    void openLanguage(Player player) {
        rootSettingsView.openLanguage(player);
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
                "Points Shop", "Your balance: " + points + " Cobblestone Points"));
        int slotIdx = 0;
        for (Map.Entry<String, ShopEntry> entry : MenuPagination.pageSlice(sorted, bounded)) {
            ShopEntry value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Item :: " + entry.getKey());
            List<String> lore = new ArrayList<>();
            lore.add(value.displayName());
            lore.add("Price: " + value.points() + " Cobblestone Points");
            if (value.service()) {
                lore.add("\u00A7dService Item — executes on purchase");
            } else {
                lore.add("Selectable quantity: 1..64");
            }
            lore.add("Click to open purchase details");
            meta.setLore(lore);
            item.setItemMeta(meta);
            MenuAction.tag(item, "shop.select", entry.getKey());
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], item);
            }
            slotIdx++;
        }
        if (sorted.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Shop Items"));
        }
        inventory.setItem(MenuLayout.SHOP_CONVERT_SLOT, MenuItems.action(
                Material.COBBLESTONE,
                "shop.convert",
                "Convert Cobblestone",
                "Converts all cobblestone in inventory to Cobblestone Points"));
        inventory.setItem(50, playerPointsItem(player));
        MenuPagination.renderControls(inventory, bounded, sorted.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
        player.openInventory(inventory);
    }

    void openShopDetail(Player player, ShopSelection selection) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SHOP_DETAIL);
        if (selection == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Item Selected", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
            MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
            player.openInventory(inventory);
            return;
        }
        ShopEntry selected = pointsService.getShopItems().get(selection.itemKey());
        if (selected == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "Unknown Item", "Return to shop list."));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
            MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
            player.openInventory(inventory);
            return;
        }
        int points = safePoints(player);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Buying: " + selected.displayName(), "Price: " + selected.points() + " Cobblestone Points"));
        inventory.setItem(13, MenuItems.named(
                selected.material(),
                "Selected :: " + selected.displayName(),
                "Price: " + selected.points() + " Cobblestone Points",
                selected.service() ? "\u00A7dService — executes on purchase" : "Direct buy amounts: 1, 2, 4, 8, 16, 32, 64",
                "Click a button to purchase immediately."));
        inventory.setItem(50, MenuItems.named(
                Material.SUNFLOWER,
                "Your Cobblestone Points",
                "Balance: " + points,
                "Price: " + selected.points() + " Cobblestone Points"));
        if (selected.service()) {
            int total = selected.points();
            boolean affordable = points >= total;
            inventory.setItem(22, MenuItems.action(
                    affordable ? Material.LIME_DYE : Material.GRAY_DYE,
                    affordable ? "shop.purchase.service" : "locked",
                    affordable ? "Purchase" : "Purchase (Locked)",
                    "Cost: " + total + " Cobblestone Points",
                    affordable ? "Click to purchase now" : "Not enough Cobblestone Points"));
        } else {
            inventory.setItem(19, quantityItem(selected.points(), points, 1));
            inventory.setItem(20, quantityItem(selected.points(), points, 2));
            inventory.setItem(21, quantityItem(selected.points(), points, 4));
            inventory.setItem(22, quantityItem(selected.points(), points, 8));
            inventory.setItem(23, quantityItem(selected.points(), points, 16));
            inventory.setItem(24, quantityItem(selected.points(), points, 32));
            inventory.setItem(25, quantityItem(selected.points(), points, 64));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.SHOP_BORDER);
        player.openInventory(inventory);
    }

    void openAchievement(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ACHIEVEMENT);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Achievements"));
        try {
            List<AchievementService.AchievementView> views = achievementService.getViews(player.getUniqueId()).values().stream().toList();
            int bounded = MenuPagination.clampPage(page, views.size());
            int slotIdx = 0;
            for (var view : MenuPagination.pageSlice(views, bounded)) {
                if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                    inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], AchievementMenuSupport.toItem(view));
                }
                slotIdx++;
            }
            if (views.isEmpty()) inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Achievements"));
            MenuPagination.renderControls(inventory, bounded, views.size());
        } catch (Exception ex) {
            player.sendMessage("Failed to load achievement: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.ACHIEVEMENT_BORDER);
        player.openInventory(inventory);
    }

    private ItemStack playerPointsItem(Player player) {
        return MenuItems.named(Material.SUNFLOWER, "Your Cobblestone Points", "Balance: " + safePoints(player));
    }

    private static ItemStack quantityItem(int pointsPerItem, int balance, int quantity) {
        int total = pointsPerItem * quantity;
        boolean affordable = balance >= total;
        return MenuItems.actionPayload(
                affordable ? Material.LIME_DYE : Material.GRAY_DYE,
                affordable ? "shop.purchase.quantity" : "locked",
                String.valueOf(quantity),
                "Buy x" + quantity,
                "Cost: " + total + " Cobblestone Points",
                affordable ? "Click to purchase now" : "Not enough Cobblestone Points");
    }

    void openProfile(Player player) {
        profileMenuView.open(player);
    }

    private int safePoints(Player player) {
        try {
            return pointsService.getBalance(player.getUniqueId());
        } catch (Exception ex) {
            return 0;
        }
    }
}
