package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void maxHomesAddsPurchasedSlotsToConfiguredBaseLimit() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        HomeService service = new HomeService(new HomeDao(database), new HomeSlotDao(database), 3);
        UUID playerId = UUID.randomUUID();

        assertEquals(3, service.maxHomes(playerId));
        assertEquals(4, service.purchaseAdditionalSlot(playerId, 0).orElseThrow());
        assertEquals(4, service.maxHomes(playerId));
        assertTrue(service.purchaseAdditionalSlot(playerId, HomeSlotCatalog.maxPurchasableSlots()).isEmpty());
    }
}
