package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.PointsDao;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PointsService {
    private final PointsDao pointsDao;
    private final EconomyOverrideDao economyOverrideDao;
    private final AuditDao auditDao;
    private final Map<String, ShopEntry> shopItems;
    private final boolean allowPartialConvert;
    private final int maxConvertPerOp;

    public PointsService(
            PointsDao pointsDao,
            EconomyOverrideDao economyOverrideDao,
            AuditDao auditDao,
            ConfigurationSection shopItemsSection,
            boolean allowPartialConvert,
            int maxConvertPerOp) throws Exception {
        this.pointsDao = pointsDao;
        this.economyOverrideDao = economyOverrideDao;
        this.auditDao = auditDao;
        this.shopItems = parseItems(shopItemsSection);
        mergeOverrides(this.shopItems, economyOverrideDao.list());
        this.allowPartialConvert = allowPartialConvert;
        this.maxConvertPerOp = maxConvertPerOp;
    }
    private static Map<String, ShopEntry> parseItems(ConfigurationSection section) {
        Map<String, ShopEntry> items = new HashMap<>();
        if (section == null) {
            return items;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            Material material = Material.matchMaterial(entry.getString("material", ""));
            if (material == null) {
                continue;
            }
            items.put(key.toLowerCase(), new ShopEntry(
                    key.toLowerCase(), material, entry.getInt("points", 1), entry.getBoolean("service", false)));
        }
        return items;
    }
    private static void mergeOverrides(Map<String, ShopEntry> baseItems, Iterable<EconomyOverrideDao.OverrideRecord> overrides) {
        for (EconomyOverrideDao.OverrideRecord override : overrides) {
            String itemKey = override.itemKey().toLowerCase();
            ShopEntry base = baseItems.get(itemKey);
            if (base == null) {
                continue;
            }
            baseItems.put(itemKey, new ShopEntry(
                    base.key(),
                    base.material(),
                    override.pointsCost(),
                    base.service()));
        }
    }
    public int getBalance(UUID playerId) throws Exception {
        return pointsDao.getBalance(playerId);
    }
    public Result convertCobblestone(Player player, int requestedAmount) throws Exception {
        if (requestedAmount <= 0) {
            return Result.fail("amount must be positive");
        }
        int capped = Math.min(requestedAmount, maxConvertPerOp);
        int available = countMaterial(player, Material.COBBLESTONE);
        if (available < capped && !allowPartialConvert) {
            return Result.fail("not enough cobblestone");
        }
        int consume = allowPartialConvert ? Math.min(capped, available) : capped;
        if (consume <= 0) {
            return Result.fail("no cobblestone available");
        }
        removeMaterial(player, Material.COBBLESTONE, consume);
        pointsDao.addPoints(player.getUniqueId(), consume, "COBBLE_CONVERT", "{\"amount\":" + consume + "}");
        return Result.ok("converted " + consume + " cobblestone into " + consume + " points", consume);
    }
    public Result purchase(Player player, String itemKey) throws Exception {
        return purchase(player, itemKey, 1);
    }
    public Result purchase(Player player, String itemKey, int quantity) throws Exception {
        if (quantity < 1 || quantity > 64) {
            return Result.fail("quantity must be in 1..64");
        }
        ShopEntry entry = shopItems.get(itemKey.toLowerCase());
        if (entry == null) {
            return Result.fail("unknown shop item");
        }
        int totalPoints;
        try {
            totalPoints = Math.multiplyExact(entry.points(), quantity);
        } catch (ArithmeticException ex) {
            return Result.fail("quantity too large");
        }
        int balance = pointsDao.getBalance(player.getUniqueId());
        if (balance < totalPoints) {
            return Result.fail("insufficient points");
        }
        if (!entry.service() && !hasInventoryCapacity(player, entry.material(), quantity)) {
            return Result.fail("not enough inventory space");
        }
        pointsDao.addPoints(player.getUniqueId(), -totalPoints, "SHOP_PURCHASE", "{\"item\":\"" + entry.key() + "\",\"quantity\":" + quantity + "}");
        if (!entry.service()) {
            addMaterial(player, entry.material(), quantity);
        }
        return Result.ok("purchased " + quantity + "x " + entry.key() + " for " + totalPoints + " points");
    }
    public Result applyOverride(Player actor, String itemKey, int newPoints) throws Exception {
        if (newPoints <= 0) {
            return Result.fail("points must be positive");
        }
        ShopEntry current = shopItems.get(itemKey.toLowerCase());
        if (current == null) {
            return Result.fail("unknown shop item");
        }
        String normalizedItemKey = itemKey.toLowerCase();
        economyOverrideDao.upsert(normalizedItemKey, newPoints, 1, actor.getUniqueId());
        ShopEntry next = new ShopEntry(current.key(), current.material(), newPoints, current.service());
        shopItems.put(normalizedItemKey, next);
        auditDao.log(actor.getUniqueId(), null, "SEASONAL_OVERRIDE_APPLIED",
                "{\"item\":\"" + itemKey + "\",\"points\":" + current.points() + "}",
                "{\"item\":\"" + itemKey + "\",\"points\":" + newPoints + "}");
        return Result.ok("seasonal override applied");
    }
    public Map<String, ShopEntry> getShopItems() {
        return Map.copyOf(shopItems);
    }
    private static int countMaterial(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                count += stack.getAmount();
            }
        }
        return count;
    }
    private static void removeMaterial(Player player, Material material, int amount) {
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
    private static boolean hasInventoryCapacity(Player player, Material material, int requiredAmount) {
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
    private static void addMaterial(Player player, Material material, int amount) {
        int remaining = amount;
        int maxStack = material.getMaxStackSize();
        while (remaining > 0) {
            int stackAmount = Math.min(maxStack, remaining);
            player.getInventory().addItem(new ItemStack(material, stackAmount));
            remaining -= stackAmount;
        }
    }
    public record Result(boolean success, String message, int amount) {
        public static Result ok(String message) { return new Result(true, message, 0); }
        public static Result ok(String message, int amount) { return new Result(true, message, amount); }
        public static Result fail(String message) { return new Result(false, message, 0); }
    }
}
