package com.lkjmcsmp.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class HomePurchaseFixture {
    final SqliteDatabase database;
    final UUID playerId = UUID.randomUUID();
    final PointsDao points;
    final HomeSlotDao slots;
    final HomeSlotPurchaseDao purchases;

    HomePurchaseFixture(Path directory, String journalMode) throws Exception {
        database = new SqliteDatabase(directory.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        try (var connection = database.open(); var statement = connection.createStatement();
             var rs = statement.executeQuery("PRAGMA journal_mode = " + journalMode)) {
            assertTrue(rs.next());
            assertEquals(journalMode, rs.getString(1).toUpperCase(java.util.Locale.ROOT));
        }
        points = new PointsDao(database);
        slots = new HomeSlotDao(database);
        purchases = new HomeSlotPurchaseDao(database);
    }

    void fund(int amount) throws Exception {
        points.addPoints(playerId, amount, "ADMIN_ADJUST", "{}");
    }

    void sql(String sql) throws Exception {
        try (var connection = database.open(); var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    long scalar(String sql) throws Exception {
        try (var connection = database.open(); var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    List<String> snapshot() throws Exception {
        var rows = new ArrayList<String>();
        try (var connection = database.open(); var statement = connection.createStatement()) {
            for (String table : List.of("player_points", "player_home_slots", "points_ledger")) {
                try (var rs = statement.executeQuery("SELECT * FROM " + table + " ORDER BY 1")) {
                    while (rs.next()) {
                        var row = new ArrayList<String>();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) row.add(rs.getString(i));
                        rows.add(table + row);
                    }
                }
            }
        }
        return rows;
    }

    void assertState(int balance, int purchased, int debits) throws Exception {
        assertEquals(balance, points.getBalance(playerId));
        assertEquals(purchased, slots.getPurchasedSlots(playerId));
        assertEquals(debits, scalar("SELECT COUNT(*) FROM points_ledger WHERE reason_code = 'HOME_SLOT_PURCHASE'"));
        assertEquals(0, scalar("SELECT COUNT(*) FROM points_ledger WHERE reason_code = 'HOME_SLOT_PURCHASE_REFUND'"));
        assertEquals(balance, scalar("SELECT COALESCE(SUM(delta), 0) FROM points_ledger"));
    }
}
