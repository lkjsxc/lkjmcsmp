package com.lkjmcsmp.gui;

import com.lkjmcsmp.progression.MilestoneStatus;
import com.lkjmcsmp.progression.ProgressionService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class ProgressMenuSupport {
    private static final String KEY_PREFIX = "Key: ";

    private ProgressMenuSupport() {
    }

    static ItemStack toItem(ProgressionService.MilestoneView view) {
        int target = Math.max(1, view.definition().target());
        int current = Math.max(0, view.progress());
        int percent = Math.min(100, (int) Math.round((current * 100.0D) / target));
        return MenuItems.named(
                statusMaterial(view.status()),
                view.definition().title(),
                KEY_PREFIX + view.definition().key(),
                "Status: " + view.status().name(),
                "Progress: " + current + "/" + target + " (" + percent + "%)",
                "Reward: " + view.definition().rewardPoints() + " points",
                view.definition().description(),
                view.status() == MilestoneStatus.COMPLETED_UNCLAIMED ? "Click to claim reward" : "Claim unavailable");
    }

    static String extractKey(ItemStack item) {
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
            return null;
        }
        for (String line : item.getItemMeta().getLore()) {
            if (line.startsWith(KEY_PREFIX)) {
                return line.substring(KEY_PREFIX.length()).trim();
            }
        }
        return null;
    }

    private static Material statusMaterial(MilestoneStatus status) {
        return switch (status) {
            case LOCKED -> Material.GRAY_DYE;
            case IN_PROGRESS -> Material.CLOCK;
            case COMPLETED_UNCLAIMED -> Material.LIME_DYE;
            case COMPLETED_CLAIMED -> Material.EMERALD;
        };
    }
}
