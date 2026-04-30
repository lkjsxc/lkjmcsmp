package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.domain.PlayerSettingsService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

final class RootSettingsMenuView {
    private final PlayerSettingsService settings;
    private final MessageService messages;

    RootSettingsMenuView(PlayerSettingsService settings, MessageService messages) {
        this.settings = settings;
        this.messages = messages;
    }

    void openRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ROOT);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(messages.get(player, "menu.root.info")));
        inventory.setItem(10, MenuItems.action(Material.ENDER_PEARL, "root.teleport", messages.get(player, "menu.root.teleport")));
        inventory.setItem(12, MenuItems.action(Material.RED_BED, "root.homes", messages.get(player, "menu.root.homes")));
        inventory.setItem(14, MenuItems.action(Material.COMPASS, "root.warps", messages.get(player, "menu.root.warps")));
        inventory.setItem(16, MenuItems.action(Material.PLAYER_HEAD, "root.team", messages.get(player, "menu.root.team")));
        inventory.setItem(20, MenuItems.action(Material.COBBLESTONE, "root.shop", messages.get(player, "menu.root.shop")));
        inventory.setItem(22, MenuItems.action(Material.BOOK, "root.achievement", messages.get(player, "menu.root.achievement")));
        inventory.setItem(24, MenuItems.action(Material.CLOCK, "root.profile", messages.get(player, "menu.root.profile")));
        inventory.setItem(28, MenuItems.action(Material.COMPARATOR, "root.settings", messages.get(player, "menu.root.settings")));
        inventory.setItem(MenuLayout.CLOSE_SLOT, MenuItems.action(Material.BARRIER, "root.close", messages.get(player, "action.close")));
        MenuDecor.fillBorder(inventory, MenuDecor.ROOT_BORDER);
        player.openInventory(inventory);
    }

    void openSettings(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.SETTINGS);
        boolean hotbar = settings.hotbarMenuEnabled(player.getUniqueId());
        String state = messages.get(player, hotbar ? "common.enabled" : "common.disabled");
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(messages.get(player, "menu.settings.info")));
        inventory.setItem(20, MenuItems.action(Material.WRITABLE_BOOK, "settings.language", messages.get(player, "menu.settings.language")));
        inventory.setItem(24, MenuItems.action(
                hotbar ? Material.NETHER_STAR : Material.GRAY_DYE,
                "settings.hotbar",
                messages.get(player, "menu.settings.hotbar"),
                messages.get(player, "menu.settings.hotbar.lore", "state", state)));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", messages.get(player, "action.back")));
        MenuDecor.fillBorder(inventory, MenuDecor.ROOT_BORDER);
        player.openInventory(inventory);
    }

    void openLanguage(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.LANGUAGE);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(messages.get(player, "menu.language.info")));
        inventory.setItem(20, MenuItems.actionPayload(
                Material.BOOK, "language.set", "en", messages.get(player, "menu.language.english")));
        inventory.setItem(24, MenuItems.actionPayload(
                Material.BOOK, "language.set", "ja", messages.get(player, "menu.language.japanese")));
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", messages.get(player, "action.back")));
        MenuDecor.fillBorder(inventory, MenuDecor.ROOT_BORDER);
        player.openInventory(inventory);
    }
}
