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
    void languageAndHotbarUpdatesPreserveActionBarAndTipsState() throws Exception {
        PlayerSettingsService service = service();
        UUID playerId = UUID.randomUUID();

        service.setActionBarEnabled(playerId, false);
        service.setTipsEnabled(playerId, false);
        service.setLanguage(playerId, "ja");
        service.setHotbarMenuEnabled(playerId, false);

        var settings = service.get(playerId);
        assertEquals("ja", settings.language());
        assertFalse(settings.hotbarMenuEnabled());
        assertFalse(settings.actionBarEnabled());
        assertFalse(settings.tipsEnabled());
    }

    @Test
    void actionBarAndTipsTogglesPreserveOtherSettings() throws Exception {
        PlayerSettingsService service = service();
        UUID playerId = UUID.randomUUID();

        service.setLanguage(playerId, "ja");
        service.setHotbarMenuEnabled(playerId, false);
        service.setTipsEnabled(playerId, false);
        var disabled = service.toggleActionBar(playerId);

        assertEquals("ja", disabled.language());
        assertFalse(disabled.hotbarMenuEnabled());
        assertFalse(disabled.actionBarEnabled());
        assertFalse(disabled.tipsEnabled());
        assertTrue(service.toggleActionBar(playerId).actionBarEnabled());
        assertTrue(service.toggleTips(playerId).tipsEnabled());
        assertEquals("ja", service.get(playerId).language());
        assertFalse(service.get(playerId).hotbarMenuEnabled());
    }

    private PlayerSettingsService service() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return new PlayerSettingsService(new PlayerSettingsDao(database), Set.of("en", "ja"), "en");
    }
}
