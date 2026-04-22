package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.plugin.temporaryend.TemporaryEndManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

final class TemporaryEndMenuView {
    private final PointsService pointsService;
    private final TemporaryEndManager temporaryEndManager;

    TemporaryEndMenuView(PointsService pointsService, TemporaryEndManager temporaryEndManager) {
        this.pointsService = pointsService;
        this.temporaryEndManager = temporaryEndManager;
    }

    void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.TEMPORARY_END);
        int points = safePoints(player);
        int cost = temporaryEndManager != null ? temporaryEndManager.cost() : 10000;
        boolean affordable = points >= cost;

        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                "Temporary End",
                "Create a temporary End dimension",
                "Duration: 3 hours",
                "Cost: " + cost + " points"));

        inventory.setItem(13, MenuItems.named(
                Material.DRAGON_EGG,
                "Temporary End Pass",
                "Creates a temporary End world",
                "Transfers nearby players (10 blocks)",
                "Defeat the Ender Dragon or find Elytra"));

        inventory.setItem(22, MenuItems.named(
                affordable ? Material.LIME_DYE : Material.BARRIER,
                affordable ? "Purchase" : "Purchase (Locked)",
                "Cost: " + cost + " points",
                affordable ? "Click to purchase now" : "Not enough points"));

        inventory.setItem(31, MenuItems.named(
                Material.SUNFLOWER,
                "Your Points",
                "Balance: " + points));

        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.TEMPORARY_END_BORDER);
        player.openInventory(inventory);
    }

    private int safePoints(Player player) {
        try {
            return pointsService.getBalance(player.getUniqueId());
        } catch (Exception ex) {
            return 0;
        }
    }
}
