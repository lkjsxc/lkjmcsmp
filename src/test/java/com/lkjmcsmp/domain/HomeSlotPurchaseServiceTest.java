package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeSlotPurchaseServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void purchaseNextSlotDeductsPointsAndIncreasesLimit() throws Exception {
        TestContext context = context();
        context.pointsDao.addPoints(context.playerId, 600, "ADMIN_ADJUST", "{}");

        var result = context.service.purchaseNext(context.player);

        assertTrue(result.success());
        assertEquals(4, context.homes.maxHomes(context.playerId));
        assertEquals(0, context.pointsDao.getBalance(context.playerId));
        assertEquals(1, context.ledgerCount("HOME_SLOT_PURCHASE"));
    }

    @Test
    void purchaseFailsBeforeMutationWhenBalanceIsTooLow() throws Exception {
        TestContext context = context();
        context.pointsDao.addPoints(context.playerId, 599, "ADMIN_ADJUST", "{}");

        var result = context.service.purchaseNext(context.player);

        assertFalse(result.success());
        assertEquals(3, context.homes.maxHomes(context.playerId));
        assertEquals(599, context.pointsDao.getBalance(context.playerId));
        assertEquals(0, context.ledgerCount("HOME_SLOT_PURCHASE"));
    }

    private TestContext context() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        PointsDao pointsDao = new PointsDao(database);
        HomeService homes = new HomeService(new HomeDao(database), new HomeSlotDao(database), 3);
        UUID playerId = UUID.randomUUID();
        return new TestContext(
                database, pointsDao, homes,
                new HomeSlotPurchaseService(pointsDao, homes),
                playerId, player(playerId));
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
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }

    private record TestContext(
            SqliteDatabase database,
            PointsDao pointsDao,
            HomeService homes,
            HomeSlotPurchaseService service,
            UUID playerId,
            Player player) {
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
