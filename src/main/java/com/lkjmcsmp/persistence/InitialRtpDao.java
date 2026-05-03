package com.lkjmcsmp.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.UUID;

public final class InitialRtpDao {
    private final SqliteDatabase database;

    public InitialRtpDao(SqliteDatabase database) {
        this.database = database;
    }

    public boolean hasCompleted(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM initial_rtp_completed WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void markCompleted(UUID playerId, Instant completedAt) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO initial_rtp_completed (player_uuid, completed_at)
                     VALUES (?, ?)
                     ON CONFLICT(player_uuid) DO NOTHING
                     """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, completedAt.toString());
            statement.executeUpdate();
        }
    }
}
