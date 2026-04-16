package com.lkjmcsmp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.UUID;

public final class PointsDao {
    private final SqliteDatabase database;

    public PointsDao(SqliteDatabase database) {
        this.database = database;
    }

    public int getBalance(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT balance FROM player_points WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void addPoints(UUID playerId, int delta, String reasonCode, String metaJson) throws Exception {
        int nextBalance = getBalance(playerId) + delta;
        if (nextBalance < 0) {
            throw new IllegalArgumentException("balance cannot go below zero");
        }
        try (var connection = database.open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement upsert = connection.prepareStatement("""
                    INSERT INTO player_points (player_uuid, balance, updated_at) VALUES (?, ?, ?)
                    ON CONFLICT(player_uuid) DO UPDATE SET balance = excluded.balance, updated_at = excluded.updated_at
                    """);
                 PreparedStatement ledger = connection.prepareStatement("""
                    INSERT INTO points_ledger (id, player_uuid, delta, reason_code, meta_json, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)) {
                String now = Instant.now().toString();
                upsert.setString(1, playerId.toString());
                upsert.setInt(2, nextBalance);
                upsert.setString(3, now);
                upsert.executeUpdate();

                ledger.setString(1, UUID.randomUUID().toString());
                ledger.setString(2, playerId.toString());
                ledger.setInt(3, delta);
                ledger.setString(4, reasonCode);
                ledger.setString(5, metaJson);
                ledger.setString(6, now);
                ledger.executeUpdate();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
