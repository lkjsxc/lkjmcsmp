package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.model.TpaRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Comparator;

final class PlayerPickerMenuView {
    void openOnlinePlayers(Player player, String title, String actionHint, boolean showManualRefresh) {
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
        if (showManualRefresh) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    void openRequesters(Player player, String title, List<TpaRequest> requests, boolean showManualRefresh) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, title);
        int slot = 0;
        for (TpaRequest request : requests) {
            Player requester = Bukkit.getPlayer(request.from());
            if (requester == null || !requester.isOnline() || slot >= MenuLayout.CONTENT_LIMIT) {
                continue;
            }
            inventory.setItem(slot++, MenuItems.named(
                    Material.PLAYER_HEAD,
                    "Requester :: " + requester.getName(),
                    request.summonHere() ? "Requested /tpahere" : "Requested /tpa"));
        }
        if (slot == 0) {
            inventory.setItem(22, MenuItems.named(Material.GRAY_DYE, "No Pending Requests"));
        }
        if (showManualRefresh) {
            inventory.setItem(MenuLayout.REFRESH_SLOT, MenuItems.named(Material.SUNFLOWER, "Refresh"));
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }
}
