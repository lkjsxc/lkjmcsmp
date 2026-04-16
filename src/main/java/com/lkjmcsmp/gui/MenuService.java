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
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, ROOT_TITLE);
        inventory.setItem(19, MenuItems.named(Material.ENDER_PEARL, "Teleport"));
        inventory.setItem(20, MenuItems.named(Material.RED_BED, "Homes"));
        inventory.setItem(21, MenuItems.named(Material.COMPASS, "Warps"));
        inventory.setItem(22, MenuItems.named(Material.PLAYER_HEAD, "Team"));
        inventory.setItem(23, MenuItems.named(Material.COBBLESTONE, "Points Shop"));
        inventory.setItem(24, MenuItems.named(Material.BOOK, "Progression"));
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.named(Material.BARRIER, "Close Menu"));
        player.openInventory(inventory);
    }

    public void openShop(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, SHOP_TITLE);
        int slot = 0;
        for (Map.Entry<String, com.lkjmcsmp.domain.model.ShopEntry> entry : pointsService.getShopItems().entrySet()) {
            if (slot >= MenuLayout.CONTENT_LIMIT) {
                break;
            }
            var value = entry.getValue();
            ItemStack item = new ItemStack(value.material(), Math.max(1, value.quantity()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(entry.getKey() + " :: " + value.points() + " pts");
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
        inventory.setItem(MenuLayout.SHOP_CONVERT_SLOT, MenuItems.named(
                Material.COBBLESTONE,
                "Convert Cobblestone",
                "Converts all cobblestone in inventory to points"));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        player.openInventory(inventory);
    }

    public void openProgress(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, PROGRESS_TITLE);
        try {
            int slot = 0;
            for (var view : progressionService.getViews(player.getUniqueId()).values()) {
                if (slot >= MenuLayout.CONTENT_LIMIT) {
                    break;
                }
                inventory.setItem(slot++, ProgressMenuSupport.toItem(view));
            }
        } catch (Exception ex) {
            player.sendMessage("Failed to load progression: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
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
                } else if (display.equals("Close Menu")) {
                    player.closeInventory();
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
                if (display.equals("Convert Cobblestone")) {
                    convertAllCobblestone(player);
                    return;
                }
                String key = display.split(" :: ")[0].trim();
                player.performCommand("shop buy " + key);
                return;
            }
            if (PROGRESS_TITLE.equals(title)) {
                String milestoneKey = ProgressMenuSupport.extractKey(event.getCurrentItem());
                if (milestoneKey == null) {
                    return;
                }
                player.sendMessage(progressionService.claim(player.getUniqueId(), milestoneKey).message());
                openProgress(player);
                return;
            }
            if (!coreMenus.handleClick(event, player, title, display)) {
                player.sendMessage("Unknown menu action.");
            }
        } catch (Exception ex) {
            player.sendMessage("Menu action failed: " + ex.getMessage());
        }
    }

    private void convertAllCobblestone(Player player) throws Exception {
        int requested = countCobblestone(player);
        if (requested <= 0) {
            player.sendMessage("no cobblestone available");
            return;
        }
        var result = pointsService.convertCobblestone(player, requested);
        if (result.success() && result.amount() > 0) {
            progressionService.increment(player.getUniqueId(), "convert_amount", result.amount());
        }
        player.sendMessage(result.message());
        openShop(player);
    }

    private static int countCobblestone(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.COBBLESTONE) {
                count += stack.getAmount();
            }
        }
        return count;
    }

}
