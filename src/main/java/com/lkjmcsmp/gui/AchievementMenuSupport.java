package com.lkjmcsmp.gui;

import com.lkjmcsmp.achievement.AchievementStatus;
import com.lkjmcsmp.achievement.AchievementService;
import com.lkjmcsmp.domain.MessageService;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class AchievementMenuSupport {
    private AchievementMenuSupport() {
    }

    static ItemStack toItem(AchievementService.AchievementView view, MessageService messages, Player player) {
        int target = Math.max(1, view.definition().target());
        int current = Math.max(0, view.progress());
        int percent = Math.min(100, (int) Math.round((current * 100.0D) / target));
        String action = view.status() == AchievementStatus.COMPLETED_UNCLAIMED ? "achievement.claim" : "locked";
        return MenuItems.actionPayload(
                statusMaterial(view.status()),
                action,
                view.definition().key(),
                messages.get(player, view.definition().titleKey()),
                messages.get(player, "achievement.lore.key", "key", view.definition().key()),
                messages.get(player, "achievement.lore.status",
                        "status", messages.get(player, statusKey(view.status()))),
                messages.get(player, "achievement.lore.progress",
                        "current", current, "target", target, "percent", percent),
                messages.get(player, "achievement.lore.reward",
                        "points", view.definition().rewardPoints()),
                messages.get(player, view.definition().descriptionKey()),
                messages.get(player, view.status() == AchievementStatus.COMPLETED_UNCLAIMED
                        ? "achievement.lore.claim-available"
                        : "achievement.lore.claim-unavailable"));
    }

    private static Material statusMaterial(AchievementStatus status) {
        return switch (status) {
            case LOCKED -> Material.GRAY_DYE;
            case IN_PROGRESS -> Material.YELLOW_DYE;
            case COMPLETED_UNCLAIMED -> Material.LIME_DYE;
            case COMPLETED_CLAIMED -> Material.EMERALD;
        };
    }

    private static String statusKey(AchievementStatus status) {
        return switch (status) {
            case LOCKED -> "achievement.status.locked";
            case IN_PROGRESS -> "achievement.status.in-progress";
            case COMPLETED_UNCLAIMED -> "achievement.status.completed-unclaimed";
            case COMPLETED_CLAIMED -> "achievement.status.completed-claimed";
        };
    }
}
