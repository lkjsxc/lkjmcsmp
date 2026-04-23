package com.lkjmcsmp.domain;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class InventoryUtil {
    private InventoryUtil() {
    }

    static int countMaterial(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    static void removeMaterial(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() != material) {
                continue;
            }
            int consume = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - consume);
            remaining -= consume;
            if (remaining == 0) {
                break;
            }
        }
    }

    static boolean hasInventoryCapacity(Player player, Material material, int requiredAmount) {
        int capacity = 0;
        int maxStack = material.getMaxStackSize();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                capacity += maxStack;
            } else if (stack.getType() == material) {
                capacity += Math.max(0, maxStack - stack.getAmount());
            }
            if (capacity >= requiredAmount) {
                return true;
            }
        }
        return capacity >= requiredAmount;
    }

    static void addMaterial(Player player, Material material, int amount) {
        int remaining = amount;
        int maxStack = material.getMaxStackSize();
        while (remaining > 0) {
            int stackAmount = Math.min(maxStack, remaining);
            player.getInventory().addItem(new ItemStack(material, stackAmount));
            remaining -= stackAmount;
        }
    }
}
