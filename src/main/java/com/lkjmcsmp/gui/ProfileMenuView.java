package com.lkjmcsmp.gui;

import com.lkjmcsmp.domain.PartyService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.achievement.AchievementService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

final class ProfileMenuView {
    private final PointsService pointsService;
    private final AchievementService achievementService;
    private final PartyService partyService;

    ProfileMenuView(PointsService pointsService, AchievementService achievementService, PartyService partyService) {
        this.pointsService = pointsService;
        this.achievementService = achievementService;
        this.partyService = partyService;
    }

    void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, MenuLayout.LARGE_CHEST_SIZE, MenuTitles.PROFILE);
        inventory.setItem(MenuLayout.INFO_PANEL_SLOT, MenuDecor.infoPanel("Profile: " + player.getName()));

        int points = safePoints(player);
        inventory.setItem(10, MenuItems.named(Material.SUNFLOWER, "Points", "Balance: " + points + " Maruishi Points"));

        try {
            var partyId = partyService.getPartyId(player.getUniqueId());
            boolean leader = partyId.isPresent() && partyService.isLeader(player.getUniqueId());
            inventory.setItem(12, MenuItems.playerHead(
                    player,
                    "Team",
                    "Party: " + partyId.orElse("<none>"),
                    "Role: " + (leader ? "leader" : partyId.isPresent() ? "member" : "none"),
                    "Click to open Team menu"));
        } catch (Exception e) {
            inventory.setItem(12, MenuItems.named(Material.BARRIER, "Team", "Failed to load team info"));
        }

        try {
            Map<String, AchievementService.AchievementView> views = achievementService.getViews(player.getUniqueId());
            long completed = views.values().stream().filter(v -> v.status().name().startsWith("COMPLETED")).count();
            long inProgress = views.values().stream().filter(v -> v.status().name().equals("IN_PROGRESS")).count();
            inventory.setItem(14, MenuItems.named(Material.BOOK, "Achievements",
                    "Completed: " + completed + "/" + views.size(),
                    "In progress: " + inProgress,
                    "Click to open Achievement menu"));
        } catch (Exception e) {
            inventory.setItem(14, MenuItems.named(Material.BARRIER, "Achievements", "Failed to load achievements"));
        }

        int minutes = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 20 / 60;
        inventory.setItem(16, MenuItems.named(Material.CLOCK, "Playtime", "Total: " + minutes + " minutes"));

        inventory.setItem(MenuLayout.BACK_SLOT, MenuItems.named(Material.ARROW, "Back"));
        MenuDecor.fillBorder(inventory, MenuDecor.PROFILE_BORDER);
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
