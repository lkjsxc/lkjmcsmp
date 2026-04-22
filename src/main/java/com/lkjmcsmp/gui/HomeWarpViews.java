package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

final class HomeWarpViews {
    private final HomeService homeService;
    private final WarpService warpService;

    HomeWarpViews(HomeService homeService, WarpService warpService) {
        this.homeService = homeService;
        this.warpService = warpService;
    }

    void openHomes(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Your Homes"));
        int slotIdx = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.named(
                        Material.RED_BED,
                        "Home :: " + home.name(),
                        "Run /home " + home.name()));
            }
            slotIdx++;
        }
        if (homes.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Homes Set"));
        }
        inventory.setItem(MenuLayout.CONTEXT_SLOT, MenuItems.named(
                Material.RESPAWN_ANCHOR,
                "Add Current Location",
                "Runs /homes addcurrent"));
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(
                Material.BARRIER,
                "Delete Homes",
                "Open dedicated deletion page"));
        MenuPagination.renderControls(inventory, bounded, homes.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.HOMES_BORDER);
        player.openInventory(inventory);
    }

    void openHomesDelete(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES_DELETE);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Delete Homes"));
        int slotIdx = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.named(
                        Material.TNT,
                        "Delete Home :: " + home.name(),
                        "Delete /home " + home.name()));
            }
            slotIdx++;
        }
        if (homes.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Homes Set"));
        }
        inventory.setItem(MenuLayout.CONTEXT_SLOT, MenuItems.named(
                Material.RED_DYE,
                "Cancel Deletion",
                "Return to Homes"));
        MenuPagination.renderControls(inventory, bounded, homes.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
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
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.named(
                        Material.COMPASS,
                        "Warp :: " + warp.name(),
                        "Run /warp " + warp.name()));
            }
            slotIdx++;
        }
        if (warps.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Warps Set"));
        }
        MenuPagination.renderControls(inventory, bounded, warps.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.WARPS_BORDER);
        player.openInventory(inventory);
    }
}
