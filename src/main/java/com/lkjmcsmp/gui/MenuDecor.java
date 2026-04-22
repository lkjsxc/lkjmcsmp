package com.lkjmcsmp.gui;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class MenuDecor {
    private static final String FILLER_NAME = " ";

    static final Material ROOT_BORDER = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
    static final Material SHOP_BORDER = Material.YELLOW_STAINED_GLASS_PANE;
    static final Material TELEPORT_BORDER = Material.PURPLE_STAINED_GLASS_PANE;
    static final Material HOMES_BORDER = Material.RED_STAINED_GLASS_PANE;
    static final Material WARPS_BORDER = Material.GREEN_STAINED_GLASS_PANE;
    static final Material TEAM_BORDER = Material.CYAN_STAINED_GLASS_PANE;
    static final Material ACHIEVEMENT_BORDER = Material.ORANGE_STAINED_GLASS_PANE;
    static final Material TEMPORARY_END_BORDER = Material.BLACK_STAINED_GLASS_PANE;
    static final Material PICKER_BORDER = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    private MenuDecor() {
    }

    static void fillBorder(Inventory inventory, Material material) {
        for (int slot = 0; slot < MenuLayout.LARGE_CHEST_SIZE; slot++) {
            if (isBorderSlot(slot) && inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler(material));
            }
        }
    }

    static void fillEmpty(Inventory inventory, Material material) {
        for (int slot = 0; slot < MenuLayout.LARGE_CHEST_SIZE; slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler(material));
            }
        }
    }

    static ItemStack infoPanel(String name, String... lore) {
        return MenuItems.named(Material.PAPER, name, lore);
    }

    private static boolean isBorderSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        return row == 0 || row == 5 || col == 0 || col == 8;
    }

    private static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(FILLER_NAME);
        item.setItemMeta(meta);
        return item;
    }
}
