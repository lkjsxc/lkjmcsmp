package com.lkjmcsmp.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

final class MenuItems {
    private MenuItems() {
    }

    static ItemStack named(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines.length > 0) {
            meta.setLore(List.of(loreLines));
        }
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack action(Material material, String action, String name, String... loreLines) {
        ItemStack item = named(material, name, loreLines);
        MenuAction.tag(item, action, "");
        return item;
    }

    static ItemStack actionPayload(Material material, String action, String payload, String name, String... loreLines) {
        ItemStack item = named(material, name, loreLines);
        MenuAction.tag(item, action, payload);
        return item;
    }

    static ItemStack playerHead(Player player, String name, String... loreLines) {
        return playerHead(player.getUniqueId(), name, loreLines);
    }

    static ItemStack playerHeadActionPayload(Player player, String action, String payload, String name, String... loreLines) {
        ItemStack item = playerHead(player, name, loreLines);
        MenuAction.tag(item, action, payload);
        return item;
    }

    static ItemStack playerHeadActionPayload(UUID playerUuid, String action, String payload, String name, String... loreLines) {
        ItemStack item = playerHead(playerUuid, name, loreLines);
        MenuAction.tag(item, action, payload);
        return item;
    }

    static ItemStack playerHead(UUID playerUuid, String name, String... loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (item.getItemMeta() instanceof SkullMeta skullMeta) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerUuid);
            skullMeta.setOwningPlayer(offline);
            skullMeta.setDisplayName(name);
            if (loreLines.length > 0) {
                skullMeta.setLore(List.of(loreLines));
            }
            item.setItemMeta(skullMeta);
        } else {
            return named(Material.PLAYER_HEAD, name, loreLines);
        }
        return item;
    }
}
