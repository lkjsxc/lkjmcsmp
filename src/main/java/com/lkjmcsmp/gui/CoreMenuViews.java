package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

final class CoreMenuViews {
    private final HomeService homeService;
    private final WarpService warpService;
    private final TeleportService teleportService;
    private final PlayerPickerMenuView pickerView;
    private final TeamMenuView teamMenuView;

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService) {
        this(homeService, warpService, partyService, teleportService, new PlayerPickerMenuView());
    }

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            PlayerPickerMenuView pickerView) {
        this.homeService = homeService;
        this.warpService = warpService;
        this.teleportService = teleportService;
        this.pickerView = pickerView;
        this.teamMenuView = new TeamMenuView(partyService);
    }

    void open(Player player, String title) throws Exception {
        switch (title) {
            case MenuTitles.TELEPORT -> openTeleport(player);
            case MenuTitles.HOMES -> openHomes(player, 0);
            case MenuTitles.HOMES_DELETE -> openHomesDelete(player, 0);
            case MenuTitles.WARPS -> openWarps(player, 0);
            case MenuTitles.TEAM -> teamMenuView.open(player);
            case MenuTitles.TEAM_DISBAND_CONFIRM -> teamMenuView.openDisbandConfirm(player);
            case MenuTitles.PICK_TPA, MenuTitles.PICK_TPA_HERE, MenuTitles.PICK_TP, MenuTitles.PICK_TP_ACCEPT, MenuTitles.PICK_INVITE ->
                    openPicker(player, title, 0);
            default -> throw new IllegalArgumentException("Unknown menu title: " + title);
        }
    }

    void openPicker(Player player, String title, int page) throws Exception {
        switch (title) {
            case MenuTitles.PICK_TPA -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TPA, "Run /tpa <player>", page, true);
            case MenuTitles.PICK_TPA_HERE -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TPA_HERE, "Run /tpahere <player>", page, true);
            case MenuTitles.PICK_TP -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TP, "Run /tp <player>", page, true);
            case MenuTitles.PICK_TP_ACCEPT -> pickerView.openRequesters(
                    player,
                    MenuTitles.PICK_TP_ACCEPT,
                    teleportService.pendingFor(player.getUniqueId()),
                    page,
                    true);
            case MenuTitles.PICK_INVITE -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_INVITE, "Run /team invite <player>", page, true);
            default -> throw new IllegalArgumentException("Unknown picker title: " + title);
        }
    }

    void openHomes(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES);
        int slot = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            inventory.setItem(slot++, MenuItems.named(
                    Material.RED_BED,
                    "Home :: " + home.name(),
                    "Run /home " + home.name()));
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
        player.openInventory(inventory);
    }

    void openHomesDelete(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> homes = homeService.list(player.getUniqueId());
        int bounded = MenuPagination.clampPage(page, homes.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES_DELETE);
        int slot = 0;
        for (var home : MenuPagination.pageSlice(homes, bounded)) {
            inventory.setItem(slot++, MenuItems.named(
                    Material.TNT,
                    "Delete Home :: " + home.name(),
                    "Delete /home " + home.name()));
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
        player.openInventory(inventory);
    }

    void openWarps(Player player, int page) throws Exception {
        List<com.lkjmcsmp.domain.model.NamedLocation> warps = warpService.list();
        int bounded = MenuPagination.clampPage(page, warps.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.WARPS);
        int slot = 0;
        for (var warp : MenuPagination.pageSlice(warps, bounded)) {
            inventory.setItem(slot++, MenuItems.named(
                    Material.COMPASS,
                    "Warp :: " + warp.name(),
                    "Run /warp " + warp.name()));
        }
        if (warps.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Warps Set"));
        }
        MenuPagination.renderControls(inventory, bounded, warps.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    private void openTeleport(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TELEPORT);
        int pendingCount = teleportService.pendingCount(player.getUniqueId());
        boolean pending = pendingCount > 0;
        boolean canDirect = player.hasPermission("lkjmcsmp.tp.use");
        inventory.setItem(10, MenuItems.named(Material.ENDER_PEARL, "Random Teleport", "Runs /rtp"));
        inventory.setItem(11, MenuItems.named(Material.COMPASS, "Request Teleport", "Pick target for /tpa"));
        inventory.setItem(12, MenuItems.named(Material.RECOVERY_COMPASS, "Request Here", "Pick target for /tpahere"));
        inventory.setItem(13, MenuItems.named(
                pending ? Material.LIME_DYE : Material.GRAY_DYE,
                pending ? "Accept Request" : "No Pending Requests",
                pendingCount > 1 ? "Runs /tpaccept (opens requester picker)" : "Runs /tpaccept"));
        inventory.setItem(14, MenuItems.named(
                pending ? Material.RED_DYE : Material.GRAY_DYE,
                pending ? "Deny Request" : "No Pending Requests",
                "Runs /tpdeny"));
        inventory.setItem(15, MenuItems.named(
                canDirect ? Material.DIAMOND_SWORD : Material.BARRIER,
                canDirect ? "Direct Teleport" : "Direct Teleport (Locked)",
                "Runs /tp <player>"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    void openTeam(Player player) throws Exception {
        teamMenuView.open(player);
    }

    void openTeamDisbandConfirm(Player player) throws Exception {
        teamMenuView.openDisbandConfirm(player);
    }

}
