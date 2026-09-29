package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.HomeSlotCatalog;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.sql.SQLException;

import static com.lkjmcsmp.persistence.HomeSlotPurchaseDao.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class HomeSlotPurchaseDaoTest {
    @TempDir Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "WAL"})
    void purchasesAllTwentyOneSlotsAtTheirFixedPrices(String mode) throws Exception {
        var f = new HomePurchaseFixture(tempDir, mode);
        int balance = HomeSlotCatalog.prices().stream().mapToInt(Integer::intValue).sum();
        f.fund(balance);
        for (int expected = 0; expected < 21; expected++) {
            int price = HomeSlotCatalog.prices().get(expected);
            var result = f.purchases.purchaseNext(f.playerId, expected);
            assertEquals(PURCHASED, result.status());
            assertEquals(expected + 1, result.purchasedSlots());
            balance -= price;
            f.assertState(balance, expected + 1, expected + 1);
            String meta = "{\"slot\":\"" + HomeSlotCatalog.keyForSlotNumber(expected + 1) + "\"}";
            try (var connection = f.database.open(); var query = connection.prepareStatement("""
                    SELECT player_uuid, delta FROM points_ledger
                    WHERE reason_code = 'HOME_SLOT_PURCHASE' AND meta_json = ?
                    """)) {
                query.setString(1, meta);
                try (var rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(f.playerId.toString(), rs.getString(1));
                    assertEquals(-price, rs.getInt(2));
                    assertFalse(rs.next());
                }
            }
        }
        assertEquals(0, balance);
        var before = f.snapshot();
        assertEquals(LIMIT_REACHED, f.purchases.purchaseNext(f.playerId, 21).status());
        assertEquals(before, f.snapshot());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "WAL"})
    void staleFutureAndInvalidRequestsLeaveEveryRowUnchanged(String mode) throws Exception {
        var f = new HomePurchaseFixture(tempDir, mode);
        f.fund(2000);
        var emptySlots = f.snapshot();
        assertEquals(ORDER_CHANGED, f.purchases.purchaseNext(f.playerId, 3).status());
        assertEquals(emptySlots, f.snapshot());
        assertEquals(PURCHASED, f.purchases.purchaseNext(f.playerId, 0).status());
        var before = f.snapshot();
        for (int expected : new int[]{-1, 0, 3, 21, Integer.MAX_VALUE}) {
            var result = f.purchases.purchaseNext(f.playerId, expected);
            assertNotEquals(PURCHASED, result.status());
            assertEquals(-1, result.purchasedSlots());
            assertEquals(before, f.snapshot());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE", "WAL"})
    void insufficientFundsDoNotLeaveNewRowsOrTimestampChanges(String mode) throws Exception {
        for (int balance : new int[]{0, 599}) {
            var f = new HomePurchaseFixture(tempDir, mode);
            if (balance > 0) f.fund(balance);
            var before = f.snapshot();
            assertEquals(INSUFFICIENT_POINTS, f.purchases.purchaseNext(f.playerId, 0).status());
            assertEquals(before, f.snapshot());
            f.assertState(balance, 0, 0);
        }
    }

    @ParameterizedTest
    @CsvSource({"DELETE,slot_insert", "WAL,slot_insert", "DELETE,slot_update", "WAL,slot_update",
            "DELETE,debit", "WAL,debit", "DELETE,ledger", "WAL,ledger", "DELETE,commit", "WAL,commit"})
    void writeAndCommitFailuresRollBackAndAllowAnExplicitRetry(String mode, String stage) throws Exception {
        var f = new HomePurchaseFixture(tempDir, mode);
        f.fund(600);
        if (stage.equals("commit")) {
            f.sql("CREATE TABLE commit_parent (id INTEGER PRIMARY KEY)");
            f.sql("CREATE TABLE commit_guard (parent_id INTEGER REFERENCES commit_parent(id)"
                    + " DEFERRABLE INITIALLY DEFERRED)");
            f.sql("""
                    CREATE TRIGGER injected_failure AFTER UPDATE ON player_home_slots
                    BEGIN INSERT INTO commit_guard VALUES (1); END
                    """);
        } else {
            String event = switch (stage) {
                case "slot_insert" -> "BEFORE INSERT ON player_home_slots";
                case "slot_update" -> "BEFORE UPDATE ON player_home_slots";
                case "debit" -> "BEFORE UPDATE ON player_points";
                case "ledger" -> "BEFORE INSERT ON points_ledger";
                default -> throw new IllegalArgumentException(stage);
            };
            f.sql("CREATE TRIGGER injected_failure " + event
                    + " BEGIN SELECT RAISE(ABORT, 'injected write failure'); END");
        }
        var before = f.snapshot();
        var failure = assertThrows(SQLException.class, () -> f.purchases.purchaseNext(f.playerId, 0));
        assertTrue(failure.getMessage().contains(stage.equals("commit")
                ? "FOREIGN KEY constraint failed" : "injected write failure"));
        assertEquals(before, f.snapshot());
        f.assertState(600, 0, 0);
        if (stage.equals("commit")) assertEquals(0, f.scalar("SELECT COUNT(*) FROM commit_guard"));
        f.sql("DROP TRIGGER injected_failure");
        assertEquals(PURCHASED, f.purchases.purchaseNext(f.playerId, 0).status());
        f.assertState(0, 1, 1);
    }
}
