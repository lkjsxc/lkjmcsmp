package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.PlayerSettingsDao;
import com.lkjmcsmp.persistence.SqliteDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSettingsServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void languageAndHotbarUpdatesPreserveActionBarState() throws Exception {
        PlayerSettingsService service = service();
        UUID playerId = UUID.randomUUID();

        service.setActionBarEnabled(playerId, false);
        service.setLanguage(playerId, "ja");
        service.setHotbarMenuEnabled(playerId, false);

        var settings = service.get(playerId);
        assertEquals("ja", settings.language());
        assertFalse(settings.hotbarMenuEnabled());
        assertFalse(settings.actionBarEnabled());
    }

    @Test
    void actionBarTogglePreservesLanguageAndHotbarState() throws Exception {
        PlayerSettingsService service = service();
        UUID playerId = UUID.randomUUID();

        service.setLanguage(playerId, "ja");
        service.setHotbarMenuEnabled(playerId, false);
        var disabled = service.toggleActionBar(playerId);

        assertEquals("ja", disabled.language());
        assertFalse(disabled.hotbarMenuEnabled());
        assertFalse(disabled.actionBarEnabled());
        assertTrue(service.toggleActionBar(playerId).actionBarEnabled());
        assertEquals("ja", service.get(playerId).language());
        assertFalse(service.get(playerId).hotbarMenuEnabled());
    }

    private PlayerSettingsService service() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return new PlayerSettingsService(new PlayerSettingsDao(database), Set.of("en", "ja"), "en");
    }
}
