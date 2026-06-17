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
        HomeSlotDao dao = dao();
        UUID playerId = UUID.randomUUID();

        assertEquals(0, dao.getPurchasedSlots(playerId));
        assertEquals(1, dao.purchaseNextSlot(playerId, 0).orElseThrow());
        assertEquals(1, dao.getPurchasedSlots(playerId));
        assertTrue(dao.purchaseNextSlot(playerId, 0).isEmpty());
        assertTrue(dao.purchaseNextSlot(playerId, 3).isEmpty());
        assertEquals(1, dao.getPurchasedSlots(playerId));
        assertEquals(2, dao.purchaseNextSlot(playerId, 1).orElseThrow());
        assertEquals(2, dao.getPurchasedSlots(playerId));
    }

    private HomeSlotDao dao() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return new HomeSlotDao(database);
    }
}
