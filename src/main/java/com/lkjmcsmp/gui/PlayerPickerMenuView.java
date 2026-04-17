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
        int slot = 0;
        for (Player online : MenuPagination.pageSlice(players, bounded)) {
            inventory.setItem(slot++, MenuItems.named(Material.PLAYER_HEAD, "Player :: " + online.getName(), actionHint));
        }
        if (players.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Players Online"));
        }
        if (showRefreshControl) {
            inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        MenuPagination.renderControls(inventory, bounded, players.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
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
        int slot = 0;
        for (TpaRequest request : MenuPagination.pageSlice(onlineRequests, bounded)) {
            Player requester = Bukkit.getPlayer(request.from());
            if (requester == null) {
                continue;
            }
            inventory.setItem(slot++, MenuItems.named(
                    Material.PLAYER_HEAD,
                    "Requester :: " + requester.getName(),
                    request.summonHere() ? "Requested /tpahere" : "Requested /tpa"));
        }
        if (onlineRequests.isEmpty()) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Pending Requests"));
        }
        if (showRefreshControl) {
            inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        MenuPagination.renderControls(inventory, bounded, onlineRequests.size());
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }
}
