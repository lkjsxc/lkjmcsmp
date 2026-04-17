package com.lkjmcsmp.gui;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.List;

final class MenuPagination {
    private MenuPagination() {
    }

    static int maxPage(int itemCount) {
        return Math.max(0, (itemCount - 1) / MenuLayout.CONTENT_LIMIT);
    }

    static int clampPage(int page, int itemCount) {
        return Math.max(0, Math.min(page, maxPage(itemCount)));
    }

    static <T> List<T> pageSlice(List<T> items, int page) {
        int bounded = clampPage(page, items.size());
        int start = bounded * MenuLayout.CONTENT_LIMIT;
        int end = Math.min(items.size(), start + MenuLayout.CONTENT_LIMIT);
        return items.subList(start, end);
    }

    static void renderControls(Inventory inventory, int page, int itemCount) {
        int bounded = clampPage(page, itemCount);
        int max = maxPage(itemCount);
        inventory.setItem(MenuLayout.PREV_PAGE_SLOT, MenuItems.named(
                bounded > 0 ? Material.ARROW : Material.GRAY_DYE,
                "Page Prev",
                bounded > 0 ? "Open previous page" : "Already at first page"));
        inventory.setItem(MenuLayout.NEXT_PAGE_SLOT, MenuItems.named(
                bounded < max ? Material.ARROW : Material.GRAY_DYE,
                "Page Next",
                bounded < max ? "Open next page" : "Already at last page"));
        inventory.setItem(MenuLayout.PAGE_INFO_SLOT, MenuItems.named(
                Material.PAPER,
                "Page :: " + (bounded + 1) + "/" + (max + 1),
                "Items: " + itemCount));
    }
}
