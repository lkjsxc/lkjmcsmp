package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import com.lkjmcsmp.persistence.HomeSlotPurchaseDao;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        new PointsDao(database).addPoints(playerId, 600, "ADMIN_ADJUST", "{}");
        var purchases = new HomeSlotPurchaseDao(database);
        var result = purchases.purchaseNext(playerId, 0);
        assertEquals(HomeSlotPurchaseDao.Status.PURCHASED, result.status());
        assertEquals(4, service.limitForPurchasedSlots(result.purchasedSlots()));
        assertEquals(4, service.maxHomes(playerId));
        assertEquals(HomeSlotPurchaseDao.Status.LIMIT_REACHED,
                purchases.purchaseNext(playerId, HomeSlotCatalog.maxPurchasableSlots()).status());
    }
}
