package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.PointsDao;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class PointsService {
    private final PointsDao pointsDao;
    private final EconomyOverrideDao economyOverrideDao;
    private final AuditDao auditDao;
    private final Map<String, ShopEntry> shopItems;
    private final Map<String, ShopEffectExecutor> effectExecutors = new java.util.HashMap<>();
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
        this.shopItems = ShopCatalog.load(shopItemsSection, economyOverrideDao.list());
        this.allowPartialConvert = allowPartialConvert;
        this.maxConvertPerOp = maxConvertPerOp;
    }

    public void registerEffect(String itemKey, ShopEffectExecutor executor) {
        effectExecutors.put(itemKey.toLowerCase(), executor);
    }

    public int getBalance(UUID playerId) throws Exception {
        return pointsDao.getBalance(playerId);
    }

    public Result convertCobblestone(Player player, int requestedAmount) throws Exception {
        if (requestedAmount <= 0) {
            return Result.fail("amount must be positive");
        }
        int capped = Math.min(requestedAmount, maxConvertPerOp);
        int available = InventoryUtil.countMaterial(player, Material.COBBLESTONE);
        if (available < capped && !allowPartialConvert) {
            return Result.fail("not enough cobblestone");
        }
        int consume = allowPartialConvert ? Math.min(capped, available) : capped;
        if (consume <= 0) {
            return Result.fail("no cobblestone available");
        }
        InventoryUtil.removeMaterial(player, Material.COBBLESTONE, consume);
        pointsDao.addPoints(player.getUniqueId(), consume, "COBBLE_CONVERT", "{\"amount\":" + consume + "}");
        return Result.ok("converted " + consume + " cobblestone into " + consume + " Points", consume);
    }

    public Result convertAllCobblestone(Player player) throws Exception {
        int available = InventoryUtil.countMaterial(player, Material.COBBLESTONE);
        if (available <= 0) return Result.fail("no cobblestone available");
        return convertCobblestone(player, available);
    }

    public Result purchase(Player player, String itemKey) throws Exception {
        return purchase(player, itemKey, 1);
    }

    public Result purchase(Player player, String itemKey, int quantity) throws Exception {
        return purchase(player, itemKey, quantity, result -> { });
    }

    public Result purchase(Player player, String itemKey, int quantity, Consumer<ShopEffectExecutor.Result> serviceCallback) throws Exception {
        if (quantity < 1 || quantity > 64) {
            return Result.fail("quantity must be in 1..64");
        }
        ShopEntry entry = shopItems.get(itemKey.toLowerCase());
        if (entry == null) {
            return Result.fail("unknown shop item");
        }
        if (entry.service() && quantity != 1) {
            return Result.fail("service items must be purchased one at a time");
        }
        int totalPoints;
        try {
            totalPoints = Math.multiplyExact(entry.points(), quantity);
        } catch (ArithmeticException ex) {
            return Result.fail("quantity too large");
        }
        int balance = pointsDao.getBalance(player.getUniqueId());
        if (balance < totalPoints) {
            return Result.fail("insufficient Points");
        }
        if (!entry.service() && !InventoryUtil.hasInventoryCapacity(player, entry.material(), quantity)) {
            return Result.fail("not enough inventory space");
        }
        pointsDao.addPoints(player.getUniqueId(), -totalPoints, "SHOP_PURCHASE", "{\"item\":\"" + entry.key() + "\",\"quantity\":" + quantity + "}");
        if (!entry.service()) {
            InventoryUtil.addMaterial(player, entry.material(), quantity);
        } else {
            ShopEffectExecutor executor = effectExecutors.get(entry.key());
            if (executor == null) {
                refundServicePurchase(player, entry, totalPoints, "missing_executor");
                return Result.fail("service item is not available");
            }
            try {
                java.util.concurrent.atomic.AtomicBoolean returned = new java.util.concurrent.atomic.AtomicBoolean(false);
                java.util.concurrent.atomic.AtomicReference<ShopEffectExecutor.Result> immediate = new java.util.concurrent.atomic.AtomicReference<>();
                executor.execute(player, entry, totalPoints, result -> {
                    if (!returned.get()) {
                        immediate.set(result);
                        return;
                    }
                    if (!result.success()) {
                        refundServicePurchase(player, entry, totalPoints, "effect_failed");
                    }
                    serviceCallback.accept(result);
                });
                returned.set(true);
                ShopEffectExecutor.Result immediateResult = immediate.get();
                if (immediateResult != null) {
                    if (!immediateResult.success()) {
                        refundServicePurchase(player, entry, totalPoints, "effect_failed");
                        return Result.fail(immediateResult.message());
                    }
                    return Result.ok(immediateResult.message());
                }
            } catch (RuntimeException ex) {
                refundServicePurchase(player, entry, totalPoints, "executor_exception");
                return Result.fail("service purchase failed: " + ex.getMessage());
            }
            return Result.pending("creating service purchase");
        }
        return Result.ok("purchased " + quantity + "x " + entry.displayName() + " for " + totalPoints + " Points");
    }

    private void refundServicePurchase(Player player, ShopEntry entry, int amount, String reason) {
        try {
            pointsDao.addPoints(player.getUniqueId(), amount, refundReason(entry), refundMeta(entry, reason));
        } catch (Exception ignored) {
        }
    }

    private static String refundReason(ShopEntry entry) {
        return entry.key().equals("temporary_dimension_pass")
                ? "TEMPORARY_DIMENSION_REFUND"
                : "SERVICE_PURCHASE_REFUND";
    }

    private static String refundMeta(ShopEntry entry, String reason) {
        return "{\"item\":\"" + entry.key() + "\",\"reason\":\"" + reason + "\"}";
    }

    public Result applyOverride(Player actor, String itemKey, int newPoints) throws Exception {
        if (newPoints <= 0) {
            return Result.fail("points must be positive");
        }
        String normalizedItemKey = itemKey.toLowerCase();
        if (HomeSlotCatalog.isHomeSlotKey(normalizedItemKey)) {
            return Result.fail("home slot prices are fixed");
        }
        ShopEntry current = shopItems.get(normalizedItemKey);
        if (current == null) {
            return Result.fail("unknown shop item");
        }
        economyOverrideDao.upsert(normalizedItemKey, newPoints, 1, actor.getUniqueId());
        ShopEntry next = new ShopEntry(current.key(), current.material(), current.displayName(), newPoints, current.service(), current.environment());
        shopItems.put(normalizedItemKey, next);
        auditDao.log(actor.getUniqueId(), null, "SEASONAL_OVERRIDE_APPLIED",
                "{\"item\":\"" + itemKey + "\",\"points\":" + current.points() + "}",
                "{\"item\":\"" + itemKey + "\",\"points\":" + newPoints + "}");
        return Result.ok("seasonal override applied");
    }

    public Map<String, ShopEntry> getShopItems() {
        return Map.copyOf(shopItems);
    }

    public record Result(Status status, String message, int amount) {
        public boolean success() { return status == Status.SUCCESS; }
        public boolean pending() { return status == Status.PENDING; }
        public static Result ok(String message) { return new Result(Status.SUCCESS, message, 0); }
        public static Result ok(String message, int amount) { return new Result(Status.SUCCESS, message, amount); }
        public static Result pending(String message) { return new Result(Status.PENDING, message, 0); }
        public static Result fail(String message) { return new Result(Status.FAILURE, message, 0); }
    }

    public enum Status { SUCCESS, PENDING, FAILURE }
}
