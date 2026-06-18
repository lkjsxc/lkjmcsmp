package com.lkjmcsmp.gui;

import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.domain.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

final class AchievementMenuView {
    private final AchievementService achievementService;
    private final MessageService messages;

    AchievementMenuView(AchievementService achievementService, MessageService messages) {
        this.achievementService = achievementService;
        this.messages = messages;
    }

    void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.ACHIEVEMENT);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel(
                messages.get(player, "achievement.menu.info")));
        try {
            List<AchievementService.AchievementView> views =
                    achievementService.getViews(player.getUniqueId()).values().stream().toList();
            int bounded = MenuPagination.clampPage(page, views.size());
            int slotIdx = 0;
            for (var view : MenuPagination.pageSlice(views, bounded)) {
                if (slotIdx < MenuLayout.CONTENT_SLOTS.length) {
                    inventory.setItem(MenuLayout.CONTENT_SLOTS[slotIdx],
                            AchievementMenuSupport.toItem(view, messages, player));
                }
                slotIdx++;
            }
            if (views.isEmpty()) {
                inventory.setItem(22, MenuItems.named(
                        Material.GRAY_DYE,
                        messages.get(player, "achievement.menu.empty")));
            }
            MenuPagination.renderControls(inventory, bounded, views.size());
        } catch (Exception ex) {
            player.sendMessage("Failed to load achievement: " + ex.getMessage());
        }
        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.action(Material.ARROW, "nav.back", "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.ACHIEVEMENT_BORDER);
        player.openInventory(inventory);
    }
}
