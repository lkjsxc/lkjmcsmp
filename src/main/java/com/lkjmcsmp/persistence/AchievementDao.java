package com.lkjmcsmp.persistence;

import com.lkjmcsmp.achievement.AchievementStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class AchievementDao {
    private final SqliteDatabase database;

    public AchievementDao(SqliteDatabase database) {
        this.database = database;
    }

    public Optional<State> get(UUID playerId, String key) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT status, progress_value FROM achievement_state
                     WHERE player_uuid = ? AND achievement_key = ?
                     """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, key);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new State(AchievementStatus.valueOf(rs.getString(1)), rs.getInt(2)));
            }
        }
    }

    public void upsert(UUID playerId, String key, AchievementStatus status, int progress) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO achievement_state (player_uuid, achievement_key, status, progress_value, updated_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(player_uuid, achievement_key) DO UPDATE SET
                      status = excluded.status, progress_value = excluded.progress_value, updated_at = excluded.updated_at
                    """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, key);
            statement.setString(3, status.name());
            statement.setInt(4, progress);
            statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    public record State(AchievementStatus status, int progress) {
    }
}
