package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.UUID;

final class CoreMenuViews {
    private final HomeService homeService;
    private final WarpService warpService;
    private final PartyService partyService;
    private final TeleportService teleportService;

    CoreMenuViews(HomeService homeService, WarpService warpService, PartyService partyService, TeleportService teleportService) {
        this.homeService = homeService;
        this.warpService = warpService;
        this.partyService = partyService;
        this.teleportService = teleportService;
    }

    void open(Player player, String title) throws Exception {
        switch (title) {
            case CoreMenuService.TELEPORT_TITLE -> openTeleport(player);
            case CoreMenuService.HOMES_TITLE -> openHomes(player);
            case CoreMenuService.WARPS_TITLE -> openWarps(player);
            case CoreMenuService.TEAM_TITLE -> openTeam(player);
            case CoreMenuService.PICK_TPA_TITLE -> openPicker(player, CoreMenuService.PICK_TPA_TITLE, "Run /tpa <player>");
            case CoreMenuService.PICK_TPA_HERE_TITLE -> openPicker(player, CoreMenuService.PICK_TPA_HERE_TITLE, "Run /tpahere <player>");
            case CoreMenuService.PICK_TP_TITLE -> openPicker(player, CoreMenuService.PICK_TP_TITLE, "Run /tp <player>");
            case CoreMenuService.PICK_INVITE_TITLE -> openPicker(player, CoreMenuService.PICK_INVITE_TITLE, "Run /team invite <player>");
            default -> throw new IllegalArgumentException("Unknown menu title: " + title);
        }
    }

    private void openTeleport(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, CoreMenuService.TELEPORT_TITLE);
        boolean pending = teleportService.pendingFor(player.getUniqueId()).isPresent();
        boolean canDirect = player.hasPermission("lkjmcsmp.tp.use");
        inventory.setItem(10, MenuItems.named(Material.ENDER_PEARL, "Random Teleport", "Runs /rtp"));
        inventory.setItem(11, MenuItems.named(Material.COMPASS, "Request Teleport", "Pick target for /tpa"));
        inventory.setItem(12, MenuItems.named(Material.RECOVERY_COMPASS, "Request Here", "Pick target for /tpahere"));
        inventory.setItem(13, MenuItems.named(pending ? Material.LIME_DYE : Material.GRAY_DYE, pending ? "Accept Request" : "No Pending Request", "Runs /tpaccept"));
        inventory.setItem(14, MenuItems.named(pending ? Material.RED_DYE : Material.GRAY_DYE, pending ? "Deny Request" : "No Pending Request", "Runs /tpdeny"));
        inventory.setItem(15, MenuItems.named(canDirect ? Material.DIAMOND_SWORD : Material.BARRIER, canDirect ? "Direct Teleport" : "Direct Teleport (Locked)", "Runs /tp <player>"));
        inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    private void openHomes(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, CoreMenuService.HOMES_TITLE);
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
        inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    private void openWarps(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, CoreMenuService.WARPS_TITLE);
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
        inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    private void openTeam(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, CoreMenuService.TEAM_TITLE);
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
        inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    private void openPicker(Player player, String title, String actionHint) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, title);
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers().stream()
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .toList()) {
            if (online.getUniqueId().equals(player.getUniqueId()) || slot >= MenuLayout.CONTENT_LIMIT) {
                continue;
            }
            inventory.setItem(slot++, MenuItems.named(Material.PLAYER_HEAD, "Player :: " + online.getName(), actionHint));
        }
        if (slot == 0) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Players Online"));
        }
        inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }
}
