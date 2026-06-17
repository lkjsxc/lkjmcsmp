package com.lkjmcsmp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.OptionalInt;
import java.util.UUID;

public final class HomeSlotDao {
    private final SqliteDatabase database;

    public HomeSlotDao(SqliteDatabase database) {
        this.database = database;
    }

    public int getPurchasedSlots(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT purchased_slots FROM player_home_slots WHERE player_uuid = ?
                     """)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public OptionalInt purchaseNextSlot(UUID playerId, int expectedPurchasedSlots) throws Exception {
        if (expectedPurchasedSlots < 0) {
            return OptionalInt.empty();
        }
        try (var connection = database.open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO player_home_slots (player_uuid, purchased_slots, updated_at)
                    VALUES (?, 0, ?)
                    ON CONFLICT(player_uuid) DO NOTHING
                    """);
                 PreparedStatement update = connection.prepareStatement("""
                    UPDATE player_home_slots
                    SET purchased_slots = purchased_slots + 1, updated_at = ?
                    WHERE player_uuid = ? AND purchased_slots = ?
                    """)) {
                String now = Instant.now().toString();
                insert.setString(1, playerId.toString());
                insert.setString(2, now);
                insert.executeUpdate();

                update.setString(1, now);
                update.setString(2, playerId.toString());
                update.setInt(3, expectedPurchasedSlots);
                if (update.executeUpdate() != 1) {
                    connection.rollback();
                    return OptionalInt.empty();
                }
                connection.commit();
                return OptionalInt.of(expectedPurchasedSlots + 1);
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
