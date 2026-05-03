package com.lkjmcsmp.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PointsDaoTest {
    @TempDir
    Path tempDir;

    @Test
    void addPointsRejectsNegativeBalanceWithoutLedger() throws Exception {
        SqliteDatabase database = database();
        PointsDao dao = new PointsDao(database);
        UUID playerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> dao.addPoints(playerId, -1, "SHOP_PURCHASE", "{}"));
        assertEquals(0, dao.getBalance(playerId));
        assertEquals(0, ledgerCount(database, playerId));
    }

    @Test
    void concurrentDeltasDoNotLoseUpdates() throws Exception {
        SqliteDatabase database = database();
        PointsDao dao = new PointsDao(database);
        UUID playerId = UUID.randomUUID();
        int workers = 8;
        int iterations = 25;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(workers);
        var executor = Executors.newFixedThreadPool(workers);

        for (int i = 0; i < workers; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < iterations; j++) {
                        dao.addPoints(playerId, 1, "COBBLE_CONVERT", "{}");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        executor.shutdownNow();
        assertEquals(workers * iterations, dao.getBalance(playerId));
        assertEquals(workers * iterations, ledgerCount(database, playerId));
    }

    private SqliteDatabase database() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return database;
    }

    private static int ledgerCount(SqliteDatabase database, UUID playerId) throws Exception {
        try (var connection = database.open();
             var statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM points_ledger WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
