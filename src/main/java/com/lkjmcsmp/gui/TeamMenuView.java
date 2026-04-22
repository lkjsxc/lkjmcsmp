package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PartyService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

final class TeamMenuView {
    private final PartyService partyService;

    TeamMenuView(PartyService partyService) {
        this.partyService = partyService;
    }

    void open(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TEAM);
        UUID playerId = player.getUniqueId();
        var partyId = partyService.getPartyId(playerId);
        boolean inTeam = partyId.isPresent();
        boolean leader = inTeam && partyService.isLeader(playerId);
        int members = inTeam ? partyService.listMembers(playerId).size() : 0;

        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Team", "Party: " + partyId.orElse("<none>")));

        inventory.setItem(10, MenuItems.named(
                Material.PLAYER_HEAD,
                "Team Info",
                "Party: " + partyId.orElse("<none>"),
                "Role: " + (leader ? "leader" : inTeam ? "member" : "none"),
                "Members: " + members));
        inventory.setItem(19, actionItem(!inTeam, Material.CRAFTING_TABLE, "Create Team", "Runs /team create", "Leave your current team first."));
        inventory.setItem(20, actionItem(leader, Material.NAME_TAG, "Invite Player", "Runs /team invite <player>", inTeam ? "Only leaders can invite players." : "Join or create a team first."));
        inventory.setItem(21, actionItem(!inTeam, Material.LIME_DYE, "Accept Invite", "Runs /team accept", "Leave your current team first."));
        inventory.setItem(23, actionItem(inTeam, Material.ENDER_PEARL, "Team Home", "Runs /team home", "Join or create a team first."));
        inventory.setItem(24, actionItem(leader, Material.RESPAWN_ANCHOR, "Set Team Home", "Runs /team sethome", inTeam ? "Only leaders can set team home." : "Join or create a team first."));
        inventory.setItem(25, actionItem(inTeam, Material.BARRIER, "Leave Team", "Runs /team leave", "Join or create a team first."));
        inventory.setItem(31, actionItem(leader, Material.TNT, "Disband Team", "Runs /team disband", inTeam ? "Only leaders can disband teams." : "Join or create a team first."));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.TEAM_BORDER);
        player.openInventory(inventory);
    }

    void openDisbandConfirm(Player player) throws Exception {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TEAM_DISBAND_CONFIRM);
        UUID playerId = player.getUniqueId();
        var partyId = partyService.getPartyId(playerId);
        boolean leader = partyService.isLeader(playerId);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Confirm Disband"));
        if (partyId.isEmpty() || !leader) {
            inventory.setItem(22, MenuItems.named(
                    Material.BARRIER,
                    "Disband Unavailable",
                    "Only current team leaders can disband."));
            inventory.setItem(32, MenuItems.named(Material.RED_DYE, "Cancel"));
            inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
            MenuDecor.fillBorder(inventory, MenuDecor.TEAM_BORDER);
            player.openInventory(inventory);
            return;
        }
        inventory.setItem(22, MenuItems.named(
                Material.PAPER,
                "Confirm Team Disband",
                "Team: " + partyId.get(),
                "This removes all team memberships and team home.",
                "This action cannot be undone."));
        inventory.setItem(30, MenuItems.named(Material.TNT, "Confirm Disband"));
        inventory.setItem(32, MenuItems.named(Material.RED_DYE, "Cancel"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.TEAM_BORDER);
        player.openInventory(inventory);
    }

    private static ItemStack actionItem(boolean enabled, Material material, String name, String commandLore, String lockedReason) {
        return enabled
                ? MenuItems.named(material, name, commandLore)
                : MenuItems.named(Material.BARRIER, name + " (Locked)", lockedReason);
    }
}
