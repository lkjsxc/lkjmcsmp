package com.lkjmcsmp.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeSlotDaoTest {
    @TempDir
    Path tempDir;

    @Test
    void startsAtZeroAndPurchasesSequentialSlotsOnly() throws Exception {
        SqliteDatabase database = database();
        HomeSlotDao dao = new HomeSlotDao(database);
        HomeSlotPurchaseDao purchases = new HomeSlotPurchaseDao(database);
        UUID playerId = UUID.randomUUID();
        new PointsDao(database).addPoints(playerId, 1380, "ADMIN_ADJUST", "{}");

        assertEquals(0, dao.getPurchasedSlots(playerId));
        assertEquals(1, purchases.purchaseNext(playerId, 0).purchasedSlots());
        assertEquals(1, dao.getPurchasedSlots(playerId));
        assertEquals(HomeSlotPurchaseDao.Status.ORDER_CHANGED, purchases.purchaseNext(playerId, 0).status());
        assertEquals(HomeSlotPurchaseDao.Status.ORDER_CHANGED, purchases.purchaseNext(playerId, 3).status());
        assertEquals(1, dao.getPurchasedSlots(playerId));
        assertEquals(2, purchases.purchaseNext(playerId, 1).purchasedSlots());
        assertEquals(2, dao.getPurchasedSlots(playerId));
    }

    @Test
    void mutationHelpersRejectAutoCommitWithoutChangingStorage() throws Exception {
        SqliteDatabase database = database();
        UUID playerId = UUID.randomUUID();
        try (var connection = database.open()) {
            assertThrows(IllegalStateException.class,
                    () -> new HomeSlotDao(database).purchaseNextSlot(connection, playerId, 0));
            assertThrows(IllegalStateException.class,
                    () -> new PointsDao(database).addPoints(connection, playerId, 600, "TEST", "{}"));
            try (var statement = connection.createStatement();
                 var rs = statement.executeQuery("SELECT (SELECT COUNT(*) FROM player_home_slots)"
                         + " + (SELECT COUNT(*) FROM player_points) + (SELECT COUNT(*) FROM points_ledger)")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    private SqliteDatabase database() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return database;
    }
}
