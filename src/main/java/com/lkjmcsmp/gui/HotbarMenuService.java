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
    private final MenuService menuService;
    private final NamespacedKey tokenKey;

    public HotbarMenuService(JavaPlugin plugin, MenuService menuService) {
        this.menuService = menuService;
        this.tokenKey = new NamespacedKey(plugin, "menu-hotbar-token");
    }

    public void install(Player player) {
        player.getInventory().setItem(HOTBAR_SLOT, createTokenItem());
    }

    public void open(Player player) {
        install(player);
        menuService.openRoot(player);
    }

    public boolean isToken(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR || item.getItemMeta() == null) {
            return false;
        }
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(tokenKey, PersistentDataType.BYTE);
        return marker != null && marker == TOKEN_VALUE;
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
