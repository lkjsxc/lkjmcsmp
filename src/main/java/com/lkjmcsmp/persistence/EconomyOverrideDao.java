package com.lkjmcsmp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EconomyOverrideDao {
    private final SqliteDatabase database;

    public EconomyOverrideDao(SqliteDatabase database) {
        this.database = database;
    }

    public List<OverrideRecord> list() throws Exception {
        List<OverrideRecord> overrides = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT item_key, points_cost, quantity
                     FROM economy_overrides
                     ORDER BY item_key
                     """);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                overrides.add(new OverrideRecord(rs.getString(1), rs.getInt(2), rs.getInt(3)));
            }
        }
        return overrides;
    }

    public void upsert(String itemKey, int pointsCost, int quantity, UUID updatedBy) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO economy_overrides (item_key, points_cost, quantity, updated_by, updated_at)
                     VALUES (?, ?, ?, ?, ?)
                     ON CONFLICT(item_key) DO UPDATE SET
                       points_cost = excluded.points_cost,
                       quantity = excluded.quantity,
                       updated_by = excluded.updated_by,
                       updated_at = excluded.updated_at
                     """)) {
            statement.setString(1, itemKey.toLowerCase());
            statement.setInt(2, pointsCost);
            statement.setInt(3, quantity);
            statement.setString(4, updatedBy == null ? "SYSTEM" : updatedBy.toString());
            statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public record OverrideRecord(String itemKey, int pointsCost, int quantity) {
    }
}
