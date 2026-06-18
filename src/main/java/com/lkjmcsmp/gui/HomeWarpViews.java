package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.HomeSlotCatalog;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.WarpService;
import com.lkjmcsmp.domain.model.ShopEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

final class HomeWarpViews {
    private final HomeService homeService;
    private final WarpService warpService;
    private final PointsService pointsService;

    HomeWarpViews(HomeService homeService, WarpService warpService, PointsService pointsService) {
        this.homeService = homeService;
        this.warpService = warpService;
        this.pointsService = pointsService;
    }

    void openHomes(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int homeLimit = homeService.maxHomes(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Your Homes",
                "Homes: " + homes.size() + " / " + homeLimit));
        int slotIdx = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.actionPayload(
                        Material.RED_BED,
                        "home.teleport",
                        home.name(),
                        "Home :: " + home.name(),
                        "Run /home " + home.name()));
            }
            slotIdx++;
        }
        if (homes.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Homes Set"));
        }
        inventory.setItem(MenuLayout.CONTEXT_SLOT, MenuItems.action(
                Material.RESPAWN_ANCHOR,
                "home.addcurrent",
                "Add Current Location",
                "Runs /homes addcurrent"));
        renderHomeSlotPurchase(player, inventory, homeLimit);
        inventory.setItem(51, MenuItems.action(
                Material.BARRIER,
                "home.delete.open",
                "Delete Homes",
                "Open dedicated deletion page"));
        MenuPagination.renderControls(inventory, bounded, homes.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.HOMES_BORDER);
        player.openInventory(inventory);
    }

    private void renderHomeSlotPurchase(Player player, Inventory inventory, int currentLimit) throws Exception {
        var next = HomeSlotCatalog.nextEntry(homeService.purchasedHomeSlots(player.getUniqueId()));
        if (next.isEmpty()) {
            inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(
                    Material.EMERALD,
                    "Home Slots Maxed",
                    "Current limit: " + currentLimit));
            return;
        }
        ShopEntry entry = next.get();
        int balance = safePoints(player);
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.action(
                balance >= entry.points() ? Material.LIME_DYE : Material.GRAY_DYE,
                "home.buy-slot",
                "Buy Next Home Slot",
                "Cost: " + entry.points() + " Cobblestone Points",
                "Balance: " + balance,
                "New limit: " + (currentLimit + 1)));
    }

    void openHomesDelete(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES_DELETE);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Delete Homes"));
        int slotIdx = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.actionPayload(
                        Material.TNT,
                        "home.delete",
                        home.name(),
                        "Delete Home :: " + home.name(),
                        "Delete /home " + home.name()));
            }
            slotIdx++;
        }
        if (homes.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Homes Set"));
        }
        inventory.setItem(MenuLayout.CONTEXT_SLOT, MenuItems.action(
                Material.RED_DYE,
                "home.delete.cancel",
                "Cancel Deletion",
                "Return to Homes"));
        MenuPagination.renderControls(inventory, bounded, homes.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.HOMES_BORDER);
        player.openInventory(inventory);
    }

    void openWarps(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> warps = warpService.list();
        int bounded = MenuPagination.clampPage(page, warps.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.WARPS);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Warps"));
        int slotIdx = 0;
        for (var warp : MenuPagination.pageSlice(warps, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.actionPayload(
                        Material.COMPASS,
                        "warp.teleport",
                        warp.name(),
                        "Warp :: " + warp.name(),
                        "Run /warp " + warp.name()));
            }
            slotIdx++;
        }
        if (warps.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Warps Set"));
        }
        MenuPagination.renderControls(inventory, bounded, warps.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.WARPS_BORDER);
        player.openInventory(inventory);
    }

    private int safePoints(Player player) {
        try {
            return pointsService.getBalance(player.getUniqueId());
        } catch (Exception ex) {
            return 0;
        }
    }
}
