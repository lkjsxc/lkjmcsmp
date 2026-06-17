package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.PlayerSettingsService;
import com.lkjmcsmp.domain.model.PlayerSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSettingsDaoTest {
    @TempDir
    Path tempDir;

    @Test
    void missingRowUsesActionBarDefaultThroughService() throws Exception {
        SqliteDatabase database = database();
        PlayerSettingsService service = new PlayerSettingsService(
                new PlayerSettingsDao(database), Set.of("en", "ja"), "en");

        assertTrue(service.actionBarEnabled(UUID.randomUUID()));
    }

    @Test
    void actionBarEnabledRoundTripsFalseAndTrue() throws Exception {
        PlayerSettingsDao dao = new PlayerSettingsDao(database());
        UUID playerId = UUID.randomUUID();

        dao.upsert(playerId, new PlayerSettings("ja", false, false));
        PlayerSettings disabled = dao.find(playerId).orElseThrow();
        assertEquals("ja", disabled.language());
        assertFalse(disabled.hotbarMenuEnabled());
        assertFalse(disabled.actionBarEnabled());

        dao.upsert(playerId, new PlayerSettings("ja", false, true));
        assertTrue(dao.find(playerId).orElseThrow().actionBarEnabled());
    }

    @Test
    void legacyPlayerSettingsTableGainsActionBarDefault() throws Exception {
        UUID playerId = UUID.randomUUID();
        Path file = tempDir.resolve(UUID.randomUUID() + ".db");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file);
             var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE player_settings (
                      player_uuid TEXT PRIMARY KEY,
                      language TEXT NOT NULL,
                      hotbar_menu_enabled INTEGER NOT NULL,
                      updated_at TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO player_settings (
                      player_uuid, language, hotbar_menu_enabled, updated_at
                    )
                    VALUES ('%s', 'ja', 0, '2026-01-01T00:00:00Z')
                    """.formatted(playerId));
        }

        SqliteDatabase database = new SqliteDatabase(file);
        database.initialize();

        assertTrue(columnExists(database, "action_bar_enabled"));
        PlayerSettings settings = new PlayerSettingsDao(database).find(playerId).orElseThrow();
        assertEquals("ja", settings.language());
        assertFalse(settings.hotbarMenuEnabled());
        assertTrue(settings.actionBarEnabled());
    }

    private SqliteDatabase database() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return database;
    }

    private static boolean columnExists(SqliteDatabase database, String column) throws Exception {
        try (var connection = database.open();
             var statement = connection.createStatement();
             var rs = statement.executeQuery("PRAGMA table_info(player_settings)")) {
            while (rs.next()) {
                if (column.equals(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
