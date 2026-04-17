package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

final class CoreMenuViews {
    private final HomeService homeService;
    private final WarpService warpService;
    private final PartyService partyService;
    private final TeleportService teleportService;
    private final PlayerPickerMenuView pickerView;
    private final boolean showManualRefreshControls;

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            boolean showManualRefreshControls) {
        this(homeService, warpService, partyService, teleportService, new PlayerPickerMenuView(), showManualRefreshControls);
    }

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            PlayerPickerMenuView pickerView,
            boolean showManualRefreshControls) {
        this.homeService = homeService;
        this.warpService = warpService;
        this.partyService = partyService;
        this.teleportService = teleportService;
        this.pickerView = pickerView;
        this.showManualRefreshControls = showManualRefreshControls;
    }

    void open(Player player, String title) throws Exception {
        switch (title) {
            case MenuTitles.TELEPORT -> openTeleport(player);
            case MenuTitles.HOMES -> openHomes(player);
            case MenuTitles.WARPS -> openWarps(player);
            case MenuTitles.TEAM -> openTeam(player);
            case MenuTitles.PICK_TPA -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TPA, "Run /tpa <player>", showManualRefreshControls);
            case MenuTitles.PICK_TPA_HERE -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TPA_HERE, "Run /tpahere <player>", showManualRefreshControls);
            case MenuTitles.PICK_TP -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_TP, "Run /tp <player>", showManualRefreshControls);
            case MenuTitles.PICK_TP_ACCEPT -> pickerView.openRequesters(
                    player,
                    MenuTitles.PICK_TP_ACCEPT,
                    teleportService.pendingFor(player.getUniqueId()),
                    showManualRefreshControls);
            case MenuTitles.PICK_INVITE -> pickerView.openOnlinePlayers(
                    player, MenuTitles.PICK_INVITE, "Run /team invite <player>", showManualRefreshControls);
            default -> throw new IllegalArgumentException("Unknown menu title: " + title);
        }
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
        if (showManualRefreshControls) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    private void openHomes(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.HOMES);
        int slot = 0;
        for (var home : homeService.list(player.getUniqueId())) {
            if (slot >= MenuLayout.CONTENT_LIMIT) {
                break;
            }
            inventory.setItem(slot++, MenuItems.named(
                    Material.RED_BED,
                    "Home :: " + home.name(),
                    "Left-click: /home " + home.name(),
                    "Right-click: /delhome " + home.name()));
        }
        if (slot == 0) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Homes Set"));
        }
        inventory.setItem(45, MenuItems.named(Material.LIME_DYE, "Set Default Home", "Runs /sethome home"));
        inventory.setItem(46, MenuItems.named(Material.RED_DYE, "Delete Default Home", "Runs /delhome home"));
        inventory.setItem(47, MenuItems.named(Material.RESPAWN_ANCHOR, "Add Current Location", "Runs /homes addcurrent"));
        if (showManualRefreshControls) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    private void openWarps(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.WARPS);
        int slot = 0;
        for (var warp : warpService.list()) {
            if (slot >= MenuLayout.CONTENT_LIMIT) {
                break;
            }
            inventory.setItem(slot++, MenuItems.named(Material.COMPASS, "Warp :: " + warp.name(), "Runs /warp " + warp.name()));
        }
        if (slot == 0) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Warps Set"));
        }
        if (showManualRefreshControls) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    private void openTeam(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TEAM);
        UUID playerId = player.getUniqueId();
        var partyId = partyService.getPartyId(playerId);
        boolean leader = partyId.isPresent() && partyService.isLeader(playerId);
        int members = partyId.isPresent() ? partyService.listMembers(playerId).size() : 0;
        inventory.setItem(10, MenuItems.named(
                Material.PLAYER_HEAD,
                "Team Info",
                "Party: " + partyId.orElse("<none>"),
                "Role: " + (leader ? "leader" : "member/none"),
                "Members: " + members));
        inventory.setItem(12, MenuItems.named(Material.CRAFTING_TABLE, "Create Team", "Runs /team create"));
        inventory.setItem(13, MenuItems.named(Material.NAME_TAG, "Invite Player", "Runs /team invite <player>"));
        inventory.setItem(14, MenuItems.named(Material.LIME_DYE, "Accept Invite", "Runs /team accept"));
        inventory.setItem(15, MenuItems.named(Material.BARRIER, "Leave Team", "Runs /team leave"));
        inventory.setItem(16, MenuItems.named(Material.ENDER_PEARL, "Team Home", "Runs /team home"));
        inventory.setItem(17, MenuItems.named(Material.RESPAWN_ANCHOR, "Set Team Home", "Runs /team sethome"));
        inventory.setItem(18, MenuItems.named(Material.TNT, "Disband Team", "Runs /team disband"));
        if (showManualRefreshControls) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }
}
