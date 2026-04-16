package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public final class MenuService {
    private static final String ROOT_TITLE = "lkjmcsmp :: menu";
    private static final String SHOP_TITLE = "lkjmcsmp :: shop";
    private static final String PROGRESS_TITLE = "lkjmcsmp :: progression";
    private final PointsService pointsService;
    private final ProgressionService progressionService;

    public MenuService(PointsService pointsService, ProgressionService progressionService) {
        this.pointsService = pointsService;
        this.progressionService = progressionService;
    }

    public void openRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, ROOT_TITLE);
        inventory.setItem(10, named(Material.ENDER_PEARL, "Teleport"));
        inventory.setItem(11, named(Material.RED_BED, "Homes"));
        inventory.setItem(12, named(Material.COMPASS, "Warps"));
        inventory.setItem(13, named(Material.PLAYER_HEAD, "Party"));
        inventory.setItem(14, named(Material.COBBLESTONE, "Points Shop"));
        inventory.setItem(15, named(Material.BOOK, "Progression"));
        player.openInventory(inventory);
    }

    public void openShop(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, SHOP_TITLE);
        int slot = 0;
        for (Map.Entry<String, com.lkjmcsmp.domain.model.ShopEntry> entry : pointsService.getShopItems().entrySet()) {
            if (slot >= 45) {
                break;
            }
            var value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), Math.max(1, value.quantity()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(entry.getKey() + " :: " + value.points() + " pts");
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
        inventory.setItem(49, named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    public void openProgress(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, PROGRESS_TITLE);
        try {
            int slot = 0;
            for (var view : progressionService.getViews(player.getUniqueId()).values()) {
                if (slot >= 45) {
                    break;
                }
                ItemStack icon = named(Material.BOOK, view.definition().title());
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(view.definition().title() + " :: " + view.status().name());
                icon.setItemMeta(meta);
                inventory.setItem(slot++, icon);
            }
        } catch (Exception ex) {
            player.sendMessage("Failed to load progression: " + ex.getMessage());
        }
        inventory.setItem(49, named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        String title = event.getView().getTitle();
        if (!title.startsWith("lkjmcsmp ::")) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String display = event.getCurrentItem().getItemMeta() == null
                ? ""
                : event.getCurrentItem().getItemMeta().getDisplayName();
        if (ROOT_TITLE.equals(title)) {
            if (display.equals("Points Shop")) {
                openShop(player);
            } else if (display.equals("Progression")) {
                openProgress(player);
            } else {
                player.performCommand(display.toLowerCase());
            }
            return;
        }
        if (display.equals("Back")) {
            openRoot(player);
            return;
        }
        if (SHOP_TITLE.equals(title)) {
            String key = display.split(" :: ")[0].trim();
            player.performCommand("shop buy " + key);
        }
    }

    private static ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
