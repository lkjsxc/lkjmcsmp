package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

final class CoreMenuViews {
    private final TeleportService teleportService;
    private final PlayerPickerMenuView pickerView;
    private final TeamMenuView teamMenuView;
    private final HomeWarpViews homeWarpViews;

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService) {
        this.teleportService = teleportService;
        this.pickerView = new PlayerPickerMenuView();
        this.teamMenuView = new TeamMenuView(partyService);
        this.homeWarpViews = new HomeWarpViews(homeService, warpService);
    }

    CoreMenuViews(
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService,
            PlayerPickerMenuView pickerView,
            TeamMenuView teamMenuView,
            HomeWarpViews homeWarpViews) {
        this.teleportService = teleportService;
        this.pickerView = pickerView;
        this.teamMenuView = teamMenuView;
        this.homeWarpViews = homeWarpViews;
    }

    void open(Player player, String title) throws Exception {
        switch (title) {
            case MenuTitles.TELEPORT -> openTeleport(player);
            case MenuTitles.HOMES -> homeWarpViews.openHomes(player, 0);
            case MenuTitles.HOMES_DELETE -> homeWarpViews.openHomesDelete(player, 0);
            case MenuTitles.WARPS -> homeWarpViews.openWarps(player, 0);
            case MenuTitles.TEAM -> teamMenuView.open(player);
            case MenuTitles.TEAM_DISBAND_CONFIRM -> teamMenuView.openDisbandConfirm(player);
            case MenuTitles.TP_DECISION -> openTeleport(player);
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
        homeWarpViews.openHomes(player, page);
    }

    void openHomesDelete(Player player, int page) throws Exception {
        homeWarpViews.openHomesDelete(player, page);
    }

    void openWarps(Player player, int page) throws Exception {
        homeWarpViews.openWarps(player, page);
    }

    private void openTeleport(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TELEPORT);
        int pendingCount = teleportService.pendingCount(player.getUniqueId());
        boolean pending = pendingCount > 0;
        boolean canDirect = player.hasPermission("lkjmcsmp.tp.use");
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Teleport"));
        inventory.setItem(10, MenuItems.action(Material.ENDER_PEARL, "teleport.rtp", "Random Teleport", "Runs /rtp"));
        inventory.setItem(11, MenuItems.action(Material.COMPASS, "teleport.pick.tpa", "Request Teleport", "Pick target for /tpa"));
        inventory.setItem(12, MenuItems.action(Material.RECOVERY_COMPASS, "teleport.pick.tpahere", "Request Here", "Pick target for /tpahere"));
        inventory.setItem(13, MenuItems.action(
                pending ? Material.LIME_DYE : Material.GRAY_DYE,
                pending ? "teleport.accept" : "teleport.none",
                pending ? "Accept Request" : "No Pending Requests",
                pendingCount > 1 ? "Runs /tpaccept (opens requester picker)" : "Runs /tpaccept"));
        inventory.setItem(14, MenuItems.action(
                pending ? Material.RED_DYE : Material.GRAY_DYE,
                pending ? "teleport.deny" : "teleport.none",
                pending ? "Deny Request" : "No Pending Requests",
                "Runs /tpdeny"));
        inventory.setItem(15, MenuItems.action(
                canDirect ? Material.DIAMOND_SWORD : Material.GRAY_DYE,
                canDirect ? "teleport.pick.direct" : "teleport.direct.locked",
                canDirect ? "Direct Teleport" : "Direct Teleport (Locked)",
                "Runs /tp <player>"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.TELEPORT_BORDER);
        player.openInventory(inventory);
    }

    void openTpDecision(Player player, java.util.UUID requesterId) {
        var request = teleportService.pendingFor(player.getUniqueId()).stream()
                .filter(pending -> pending.from().equals(requesterId))
                .findFirst();
        Player requester = Bukkit.getPlayer(requesterId);
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TP_DECISION);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Teleport Request"));
        if (request.isEmpty() || requester == null) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Pending Requests"));
        } else {
            String direction = request.get().summonHere() ? "Requested /tpahere" : "Requested /tpa";
            inventory.setItem(13, MenuItems.playerHead(requester, "Requester :: " + requester.getName(), direction));
            inventory.setItem(21, MenuItems.actionPayload(
                    Material.LIME_DYE, "tpdecision.accept", requesterId.toString(), "Accept"));
            inventory.setItem(23, MenuItems.actionPayload(
                    Material.RED_DYE, "tpdecision.deny", requesterId.toString(), "Deny"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.TELEPORT_BORDER);
        player.openInventory(inventory);
    }

    void openTeam(Player player) throws Exception {
        teamMenuView.open(player);
    }

    void openTeamDisbandConfirm(Player player) throws Exception {
        teamMenuView.openDisbandConfirm(player);
    }
}
