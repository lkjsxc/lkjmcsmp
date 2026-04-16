package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.AuditDao;
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
    private final AuditDao auditDao;
    private final Map<String, ShopEntry> shopItems;
    private final boolean allowPartialConvert;
    private final int maxConvertPerOp;

    public PointsService(
            PointsDao pointsDao,
            AuditDao auditDao,
            ConfigurationSection shopItemsSection,
            boolean allowPartialConvert,
            int maxConvertPerOp) {
        this.pointsDao = pointsDao;
        this.auditDao = auditDao;
        this.shopItems = parseItems(shopItemsSection);
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
                    key.toLowerCase(), material, entry.getInt("quantity", 1), entry.getInt("points", 1)));
        }
        return items;
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
        return Result.ok("converted " + consume + " cobblestone into " + consume + " points");
    }

    public Result purchase(Player player, String itemKey) throws Exception {
        ShopEntry entry = shopItems.get(itemKey.toLowerCase());
        if (entry == null) {
            return Result.fail("unknown shop item");
        }
        int balance = pointsDao.getBalance(player.getUniqueId());
        if (balance < entry.points()) {
            return Result.fail("insufficient points");
        }
        ItemStack stack = new ItemStack(entry.material(), entry.quantity());
        Map<Integer, ItemStack> rejected = player.getInventory().addItem(stack);
        if (!rejected.isEmpty()) {
            return Result.fail("not enough inventory space");
        }
        pointsDao.addPoints(player.getUniqueId(), -entry.points(), "SHOP_PURCHASE", "{\"item\":\"" + entry.key() + "\"}");
        return Result.ok("purchased " + entry.quantity() + "x " + entry.material() + " for " + entry.points() + " points");
    }

    public Result applyOverride(Player actor, String itemKey, int newPoints, int newQty) throws Exception {
        ShopEntry current = shopItems.get(itemKey.toLowerCase());
        if (current == null) {
            return Result.fail("unknown shop item");
        }
        ShopEntry next = new ShopEntry(current.key(), current.material(), newQty, newPoints);
        shopItems.put(itemKey.toLowerCase(), next);
        auditDao.log(
                actor.getUniqueId(),
                null,
                "SEASONAL_OVERRIDE_APPLIED",
                "{\"item\":\"" + itemKey + "\",\"points\":" + current.points() + ",\"qty\":" + current.quantity() + "}",
                "{\"item\":\"" + itemKey + "\",\"points\":" + newPoints + ",\"qty\":" + newQty + "}");
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

    public record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
