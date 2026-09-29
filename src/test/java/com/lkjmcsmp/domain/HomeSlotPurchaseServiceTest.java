package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import com.lkjmcsmp.persistence.HomeSlotPurchaseDao;
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

    @Test
    void failedSlotWriteLeavesAllPurchaseTablesUnchanged() throws Exception {
        TestContext context = context();
        context.pointsDao.addPoints(context.playerId, 600, "ADMIN_ADJUST", "{}");
        context.sql("""
                CREATE TRIGGER reject_slot BEFORE UPDATE ON player_home_slots
                BEGIN SELECT RAISE(ABORT, 'injected slot failure'); END
                """);
        var before = context.snapshot();

        var failure = assertThrows(java.sql.SQLException.class,
                () -> context.service.purchaseNext(context.player));

        assertTrue(failure.getMessage().contains("injected slot failure"));
        assertEquals(before, context.snapshot());
    }

    @Test
    void failedCommitLeavesAllPurchaseTablesUnchanged() throws Exception {
        TestContext context = context();
        context.pointsDao.addPoints(context.playerId, 600, "ADMIN_ADJUST", "{}");
        context.sql("CREATE TABLE commit_parent (id INTEGER PRIMARY KEY)");
        context.sql("""
                CREATE TABLE commit_guard (parent_id INTEGER REFERENCES commit_parent(id)
                DEFERRABLE INITIALLY DEFERRED)
                """);
        context.sql("""
                CREATE TRIGGER reject_commit AFTER UPDATE ON player_home_slots
                BEGIN INSERT INTO commit_guard VALUES (1); END
                """);
        var before = context.snapshot();

        var failure = assertThrows(java.sql.SQLException.class,
                () -> context.service.purchaseNext(context.player));

        assertTrue(failure.getMessage().contains("FOREIGN KEY constraint failed"));
        assertEquals(before, context.snapshot());
    }

    private TestContext context() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        PointsDao pointsDao = new PointsDao(database);
        HomeService homes = new HomeService(new HomeDao(database), new HomeSlotDao(database), 3);
        UUID playerId = UUID.randomUUID();
        return new TestContext(
                database, pointsDao, homes,
                new HomeSlotPurchaseService(new HomeSlotPurchaseDao(database), homes),
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
        void sql(String sql) throws Exception {
            try (var connection = database.open(); var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }

        java.util.List<String> snapshot() throws Exception {
            var rows = new java.util.ArrayList<String>();
            try (var connection = database.open(); var statement = connection.createStatement()) {
                for (String table : java.util.List.of("player_points", "player_home_slots", "points_ledger")) {
                    try (var rs = statement.executeQuery("SELECT * FROM " + table + " ORDER BY 1")) {
                        while (rs.next()) {
                            var row = new java.util.ArrayList<String>();
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) row.add(rs.getString(i));
                            rows.add(table + row);
                        }
                    }
                }
            }
            return rows;
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
