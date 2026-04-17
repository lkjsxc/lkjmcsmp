package com.lkjmcsmp.gui;

import org.bukkit.entity.Player;

final class MenuPageStateSync {
    private MenuPageStateSync() {
    }

    static int readCurrentPage(Player player, int fallback) {
        var item = player.getOpenInventory().getTopInventory().getItem(MenuLayout.PAGE_INFO_SLOT);
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
            return fallback;
        }
        String name = item.getItemMeta().getDisplayName();
        if (!name.startsWith("Page :: ")) {
            return fallback;
        }
        int slash = name.indexOf('/');
        if (slash <= "Page :: ".length()) {
            return fallback;
        }
        try {
            return Math.max(0, Integer.parseInt(name.substring("Page :: ".length(), slash)) - 1);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
