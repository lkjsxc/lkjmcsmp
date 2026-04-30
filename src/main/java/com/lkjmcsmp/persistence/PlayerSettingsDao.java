package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.PlayerSettings;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PlayerSettingsDao {
    private final SqliteDatabase database;

    public PlayerSettingsDao(SqliteDatabase database) {
        this.database = database;
    }

    public Optional<PlayerSettings> find(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT language, hotbar_menu_enabled FROM player_settings WHERE player_uuid = ?
                     """)) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PlayerSettings(rs.getString(1), rs.getInt(2) == 1));
            }
        }
    }

    public void upsert(UUID playerId, PlayerSettings settings) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO player_settings (player_uuid, language, hotbar_menu_enabled, updated_at)
                     VALUES (?, ?, ?, ?)
                     ON CONFLICT(player_uuid) DO UPDATE SET
                       language=excluded.language,
                       hotbar_menu_enabled=excluded.hotbar_menu_enabled,
                       updated_at=excluded.updated_at
                     """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, settings.language());
            statement.setInt(3, settings.hotbarMenuEnabled() ? 1 : 0);
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
        }
    }
}
