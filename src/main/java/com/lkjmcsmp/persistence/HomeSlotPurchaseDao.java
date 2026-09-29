package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.HomeSlotCatalog;

import java.util.Objects;
import java.util.UUID;

public final class HomeSlotPurchaseDao {
    private final SqliteDatabase database;
    private final HomeSlotDao slots;
    private final PointsDao points;

    public HomeSlotPurchaseDao(SqliteDatabase database) {
        this.database = database;
        this.slots = new HomeSlotDao(database);
        this.points = new PointsDao(database);
    }

    public Result purchaseNext(UUID playerId, int expectedPurchasedSlots) throws Exception {
        Objects.requireNonNull(playerId, "playerId");
        if (expectedPurchasedSlots < 0) {
            return Result.failed(Status.ORDER_CHANGED);
        }
        var next = HomeSlotCatalog.nextEntry(expectedPurchasedSlots);
        if (next.isEmpty()) {
            return Result.failed(Status.LIMIT_REACHED);
        }
        var entry = next.get();
        try (var connection = database.open()) {
            connection.setAutoCommit(false);
            try {
                // Start with a write, not a read snapshot that another writer can invalidate.
                var purchased = slots.purchaseNextSlot(connection, playerId, expectedPurchasedSlots);
                if (purchased.isEmpty()) {
                    connection.rollback();
                    return Result.failed(Status.ORDER_CHANGED);
                }
                if (points.getBalance(connection, playerId) < entry.points()) {
                    connection.rollback();
                    return Result.failed(Status.INSUFFICIENT_POINTS);
                }
                points.addPoints(connection, playerId, -entry.points(), "HOME_SLOT_PURCHASE",
                        "{\"slot\":\"" + entry.key() + "\"}");
                connection.commit();
                return new Result(Status.PURCHASED, purchased.getAsInt());
            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (Exception rollback) {
                    ex.addSuppressed(rollback);
                }
                throw ex;
            }
        }
    }

    public enum Status { PURCHASED, ORDER_CHANGED, INSUFFICIENT_POINTS, LIMIT_REACHED }

    public record Result(Status status, int purchasedSlots) {
        private static Result failed(Status status) {
            return new Result(status, -1);
        }
    }
}
