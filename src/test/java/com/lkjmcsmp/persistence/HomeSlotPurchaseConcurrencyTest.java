package com.lkjmcsmp.persistence;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.lkjmcsmp.persistence.HomeSlotPurchaseDao.Status.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class HomeSlotPurchaseConcurrencyTest {
    @TempDir Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "WAL"})
    void onlyOneConcurrentRequestCanBuyTheSameExpectedSlot(String mode) throws Exception {
        var f = new HomePurchaseFixture(tempDir, mode);
        f.fund(10000);
        int contenders = 8;
        var ready = new CountDownLatch(contenders);
        var start = new CountDownLatch(1);
        var results = new ArrayList<HomeSlotPurchaseDao.Result>();
        try (var executor = Executors.newFixedThreadPool(contenders)) {
            var futures = new ArrayList<Future<HomeSlotPurchaseDao.Result>>();
            for (int i = 0; i < contenders; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(10, SECONDS));
                    return new HomeSlotPurchaseDao(f.database).purchaseNext(f.playerId, 0);
                }));
            }
            assertTrue(ready.await(10, SECONDS));
            start.countDown();
            for (var future : futures) results.add(future.get(10, SECONDS));
        }
        assertEquals(1, results.stream().filter(r -> r.status() == PURCHASED).count());
        assertEquals(contenders - 1, results.stream().filter(r -> r.status() == ORDER_CHANGED).count());
        f.assertState(9400, 1, 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "WAL"})
    void homePurchaseAndAnotherDebitCannotSpendTheSameBalance(String mode) throws Exception {
        var f = new HomePurchaseFixture(tempDir, mode);
        f.fund(600);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        HomeSlotPurchaseDao.Result purchase;
        boolean otherDebit;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var home = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, SECONDS));
                return new HomeSlotPurchaseDao(f.database).purchaseNext(f.playerId, 0);
            });
            var other = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, SECONDS));
                try {
                    new PointsDao(f.database).addPoints(f.playerId, -600, "SHOP_PURCHASE", "{}");
                    return true;
                } catch (IllegalArgumentException insufficient) {
                    assertTrue(insufficient.getMessage().contains("balance cannot go below zero"));
                    return false;
                }
            });
            assertTrue(ready.await(10, SECONDS));
            start.countDown();
            purchase = home.get(10, SECONDS);
            otherDebit = other.get(10, SECONDS);
        }
        boolean homeWon = purchase.status() == PURCHASED;
        assertNotEquals(homeWon, otherDebit);
        assertEquals(homeWon ? PURCHASED : INSUFFICIENT_POINTS, purchase.status());
        f.assertState(0, homeWon ? 1 : 0, homeWon ? 1 : 0);
        assertEquals(2, f.scalar("SELECT COUNT(*) FROM points_ledger"));
    }
}
