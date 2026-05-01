package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.model.TpaRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.List;

final class PlayerPickerMenuView {
    void openOnlinePlayers(Player player, String title, String actionHint, int page, boolean showRefreshControl) {
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(online -> !online.getUniqueId().equals(player.getUniqueId()))
                .map(online -> (Player) online)
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        int bounded = MenuPagination.clampPage(page, players.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, title);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Select Player"));
        int slotIdx = 0;
        for (Player online : MenuPagination.pageSlice(players, bounded)) {
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.playerHeadActionPayload(
                        online, "picker.player", online.getName(), "Player :: " + online.getName(), actionHint));
            }
            slotIdx++;
        }
        if (players.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Players Online"));
        }
        if (showRefreshControl) {
            inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.action(Material.SUNFLOWER, "picker.refresh", "Refresh"));
        }
        MenuPagination.renderControls(inventory, bounded, players.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.PICKER_BORDER);
        player.openInventory(inventory);
    }

    void openRequesters(Player player, String title, List<TpaRequest> requests, int page, boolean showRefreshControl) {
        List<TpaRequest> onlineRequests = requests.stream()
                .filter(request -> {
                    Player requester = Bukkit.getPlayer(request.from());
                    return requester != null && requester.isOnline();
                })
                .toList();
        int bounded = MenuPagination.clampPage(page, onlineRequests.size());
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, title);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Select Requester"));
        int slotIdx = 0;
        for (TpaRequest request : MenuPagination.pageSlice(onlineRequests, bounded)) {
            Player requester = Bukkit.getPlayer(request.from());
            if (requester == null) {
                continue;
            }
            if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx], MenuItems.playerHeadActionPayload(
                        requester,
                        "picker.requester",
                        requester.getName(),
                        "Requester :: " + requester.getName(),
                        request.summonHere() ? "Requested /tpahere" : "Requested /tpa"));
            }
            slotIdx++;
        }
        if (onlineRequests.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Pending Requests"));
        }
        if (showRefreshControl) {
            inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.action(Material.SUNFLOWER, "picker.refresh", "Refresh"));
        }
        MenuPagination.renderControls(inventory, bounded, onlineRequests.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.PICKER_BORDER);
        player.openInventory(inventory);
    }
}
