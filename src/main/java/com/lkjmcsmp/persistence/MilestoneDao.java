package com.lkjmcsmp.persistence;

import com.lkjmcsmp.progression.MilestoneStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class MilestoneDao {
    private final SqliteDatabase database;

    public MilestoneDao(SqliteDatabase database) {
        this.database = database;
    }

    public Optional<State> get(UUID playerId, String key) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT status, progress_value FROM milestone_state
                     WHERE player_uuid = ? AND milestone_key = ?
                     """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, key);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new State(MilestoneStatus.valueOf(rs.getString(1)), rs.getInt(2)));
            }
        }
    }

    public void upsert(UUID playerId, String key, MilestoneStatus status, int progress) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO milestone_state (player_uuid, milestone_key, status, progress_value, updated_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(player_uuid, milestone_key) DO UPDATE SET
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

    public record State(MilestoneStatus status, int progress) {
    }
}
