package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.HomeService;
import com.lkjmcsmp.domain.HomeSlotCatalog;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.persistence.AuditDao;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HomeSlotEffectExecutorTest {
    @TempDir
    Path tempDir;

    @Test
    void servicePurchaseUnlocksNextSlotAndRefundsFutureSlot() throws Exception {
        TestContext context = context();
        context.registerHomeSlotExecutor();
        context.grantPoints(10000);

        var first = context.points.purchase(context.player, "home_slot_01", 1);

        assertTrue(first.success());
        assertEquals(4, context.homes.maxHomes(context.playerId));
        assertEquals(7600, context.balance());

        AtomicInteger callbacks = new AtomicInteger();
        var future = context.points.purchase(
                context.player,
                "home_slot_03",
                1,
                result -> callbacks.incrementAndGet());

        assertFalse(future.success());
        assertEquals(0, callbacks.get());
        assertEquals(4, context.homes.maxHomes(context.playerId));
        assertEquals(7600, context.balance());
        assertEquals(1, context.ledgerCount("SERVICE_PURCHASE_REFUND"));
    }

    @Test
    void overrideCannotChangeFixedHomeSlotPrices() throws Exception {
        TestContext context = context();

        var result = context.points.applyOverride(player(UUID.randomUUID()), "home_slot_01", 1);

        assertFalse(result.success());
        assertEquals(2400, context.points.getShopItems().get("home_slot_01").points());
    }

    private TestContext context() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        PointsDao pointsDao = new PointsDao(database);
        HomeService homes = new HomeService(new HomeDao(database), new HomeSlotDao(database), 3);
        PointsService points = new PointsService(
                pointsDao,
                new EconomyOverrideDao(database),
                new AuditDao(database),
                new YamlConfiguration().createSection("items"),
                false,
                4096);
        UUID playerId = UUID.randomUUID();
        return new TestContext(database, pointsDao, homes, points, playerId, player(playerId));
    }

    private static Player player(UUID playerId) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> playerId;
                    case "getName" -> "home-slot-test";
                    case "isOnline" -> true;
                    case "toString" -> "Player(" + playerId + ")";
                    case "hashCode" -> playerId.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    private record TestContext(
            SqliteDatabase database,
            PointsDao pointsDao,
            HomeService homes,
            PointsService points,
            UUID playerId,
            Player player) {
        void registerHomeSlotExecutor() {
            HomeSlotEffectExecutor executor = new HomeSlotEffectExecutor(homes);
            HomeSlotCatalog.entries().keySet().forEach(key -> points.registerEffect(key, executor));
        }

        void grantPoints(int amount) throws Exception {
            pointsDao.addPoints(playerId, amount, "ADMIN_ADJUST", "{}");
        }

        int balance() throws Exception {
            return pointsDao.getBalance(playerId);
        }

        int ledgerCount(String reasonCode) throws Exception {
            try (var connection = database.open();
                 var statement = connection.prepareStatement("""
                         SELECT COUNT(*) FROM points_ledger WHERE player_uuid = ? AND reason_code = ?
                         """)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, reasonCode);
                try (var rs = statement.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }
}
