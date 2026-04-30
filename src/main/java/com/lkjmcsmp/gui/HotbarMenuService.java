package com.lkjmcsmp.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class HotbarMenuService {
    public static final int HOTBAR_SLOT = 8;
    private static final byte TOKEN_VALUE = 1;
    private final JavaPlugin plugin;
    private final MenuService menuService;
    private final NamespacedKey tokenKey;

    public HotbarMenuService(JavaPlugin plugin, MenuService menuService) {
        this.plugin = plugin;
        this.menuService = menuService;
        this.tokenKey = new NamespacedKey(plugin, "menu-hotbar-token");
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public void install(Player player) {
        removeStrayTokens(player);
        player.getInventory().setItem(HOTBAR_SLOT, createTokenItem());
    }

    public void ensureInstalled(Player player) {
        if (!isToken(player.getInventory().getItem(HOTBAR_SLOT))) {
            install(player);
        } else {
            removeStrayTokens(player);
        }
    }

    public void open(Player player) {
        ensureInstalled(player);
        menuService.openRoot(player);
    }

    public void openFromInventoryInteraction(Player player) {
        ensureInstalled(player);
        clearGhostCursorToken(player);
        menuService.openRoot(player);
        resyncNextTick(player);
    }

    public void resyncAfterMenuClose(Player player) {
        resyncNextTick(player);
    }

    public void syncSoon(Player player) {
        resyncDelayed(player, 1L);
        resyncDelayed(player, 5L);
    }

    public boolean isToken(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR || item.getItemMeta() == null) {
            return false;
        }
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(tokenKey, PersistentDataType.BYTE);
        return marker != null && marker == TOKEN_VALUE;
    }

    private void resyncNextTick(Player player) {
        resyncDelayed(player, 1L);
    }

    private void resyncDelayed(Player player, long delayTicks) {
        player.getScheduler().runDelayed(plugin, task -> {
            ensureInstalled(player);
            clearGhostCursorToken(player);
            player.updateInventory();
        }, null, delayTicks);
    }

    private void clearGhostCursorToken(Player player) {
        if (isToken(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
        }
    }

    private void removeStrayTokens(Player player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot != HOTBAR_SLOT && isToken(inventory.getItem(slot))) {
                inventory.setItem(slot, null);
            }
        }
        clearGhostCursorToken(player);
    }

    private ItemStack createTokenItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Open Menu");
        meta.setLore(java.util.List.of("Click, drop, or click in inventory to open /menu"));
        meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.BYTE, TOKEN_VALUE);
        item.setItemMeta(meta);
        return item;
    }
}
