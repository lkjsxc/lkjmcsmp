package com.lkjmcsmp.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

final class MenuAction {
    private static final NamespacedKey ACTION_KEY = NamespacedKey.fromString("lkjmcsmp:menu_action");
    private static final NamespacedKey PAYLOAD_KEY = NamespacedKey.fromString("lkjmcsmp:menu_payload");

    private MenuAction() {
    }

    static void tag(ItemStack item, String action, String payload) {
        if (item == null || item.getItemMeta() == null || ACTION_KEY == null || PAYLOAD_KEY == null) {
            return;
        }
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        if (payload != null && !payload.isBlank()) {
            meta.getPersistentDataContainer().set(PAYLOAD_KEY, PersistentDataType.STRING, payload);
        }
        item.setItemMeta(meta);
    }

    static String action(ItemStack item) {
        if (item == null || item.getItemMeta() == null || ACTION_KEY == null) {
            return "";
        }
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(ACTION_KEY, PersistentDataType.STRING, "");
    }

    static String payload(ItemStack item) {
        if (item == null || item.getItemMeta() == null || PAYLOAD_KEY == null) {
            return "";
        }
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(PAYLOAD_KEY, PersistentDataType.STRING, "");
    }
}
