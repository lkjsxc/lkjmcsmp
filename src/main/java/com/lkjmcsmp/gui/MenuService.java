package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportService;
import com.lkjmcsmp.domain.WarpService;
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
    static final String ROOT_TITLE = "lkjmcsmp :: menu";
    static final String SHOP_TITLE = "lkjmcsmp :: shop";
    static final String PROGRESS_TITLE = "lkjmcsmp :: progression";
    private final PointsService pointsService;
    private final ProgressionService progressionService;
    private final CoreMenuService coreMenus;

    public MenuService(
            PointsService pointsService,
            ProgressionService progressionService,
            HomeService homeService,
            WarpService warpService,
            PartyService partyService,
            TeleportService teleportService) {
        this.pointsService = pointsService;
        this.progressionService = progressionService;
        CoreMenuViews coreViews = new CoreMenuViews(homeService, warpService, partyService, teleportService);
        this.coreMenus = new CoreMenuService(coreViews, this::openRoot);
    }

    public void openRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, ROOT_TITLE);
        inventory.setItem(10, MenuItems.named(Material.ENDER_PEARL, "Teleport"));
        inventory.setItem(11, MenuItems.named(Material.RED_BED, "Homes"));
        inventory.setItem(12, MenuItems.named(Material.COMPASS, "Warps"));
        inventory.setItem(13, MenuItems.named(Material.PLAYER_HEAD, "Team"));
        inventory.setItem(14, MenuItems.named(Material.COBBLESTONE, "Points Shop"));
        inventory.setItem(15, MenuItems.named(Material.BOOK, "Progression"));
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
        inventory.setItem(49, MenuItems.named(Material.BARRIER, "Back"));
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
                ItemStack icon = MenuItems.named(Material.BOOK, view.definition().title());
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(view.definition().title() + " :: " + view.status().name());
                icon.setItemMeta(meta);
                inventory.setItem(slot++, icon);
            }
        } catch (Exception ex) {
            player.sendMessage("Failed to load progression: " + ex.getMessage());
        }
        inventory.setItem(49, MenuItems.named(Material.BARRIER, "Back"));
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
        try {
            if (ROOT_TITLE.equals(title)) {
                if (display.equals("Points Shop")) {
                    openShop(player);
                } else if (display.equals("Progression")) {
                    openProgress(player);
                } else if (!coreMenus.openFromRoot(player, display)) {
                    player.sendMessage("Unknown menu action.");
                }
                return;
            }
            if (display.equals("Back")) {
                if (!coreMenus.openBack(player, title)) {
                    openRoot(player);
                }
                return;
            }
            if (SHOP_TITLE.equals(title)) {
                String key = display.split(" :: ")[0].trim();
                player.performCommand("shop buy " + key);
                return;
            }
            if (PROGRESS_TITLE.equals(title)) {
                return;
            }
            if (!coreMenus.handleClick(event, player, title, display)) {
                player.sendMessage("Unknown menu action.");
            }
        } catch (Exception ex) {
            player.sendMessage("Menu action failed: " + ex.getMessage());
        }
    }

}
