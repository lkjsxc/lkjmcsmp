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

    public int addPoints(UUID playerId, int delta, String reasonCode, String metaJson) throws Exception {
        try (var connection = database.open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO player_points (player_uuid, balance, updated_at)
                    VALUES (?, 0, ?)
                    ON CONFLICT(player_uuid) DO NOTHING
                    """);
                 PreparedStatement update = connection.prepareStatement("""
                    UPDATE player_points
                    SET balance = balance + ?, updated_at = ?
                    WHERE player_uuid = ?
                    """);
                 PreparedStatement balance = connection.prepareStatement("""
                    SELECT balance FROM player_points WHERE player_uuid = ?
                    """);
                 PreparedStatement ledger = connection.prepareStatement("""
                    INSERT INTO points_ledger (id, player_uuid, delta, reason_code, meta_json, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)) {
                String now = Instant.now().toString();
                insert.setString(1, playerId.toString());
                insert.setString(2, now);
                insert.executeUpdate();

                update.setInt(1, delta);
                update.setString(2, now);
                update.setString(3, playerId.toString());
                update.executeUpdate();

                balance.setString(1, playerId.toString());
                int nextBalance;
                try (ResultSet rs = balance.executeQuery()) {
                    nextBalance = rs.next() ? rs.getInt(1) : 0;
                }
                if (nextBalance < 0) {
                    throw new IllegalArgumentException("balance cannot go below zero");
                }

                ledger.setString(1, UUID.randomUUID().toString());
                ledger.setString(2, playerId.toString());
                ledger.setInt(3, delta);
                ledger.setString(4, reasonCode);
                ledger.setString(5, metaJson);
                ledger.setString(6, now);
                ledger.executeUpdate();
                connection.commit();
                return nextBalance;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
