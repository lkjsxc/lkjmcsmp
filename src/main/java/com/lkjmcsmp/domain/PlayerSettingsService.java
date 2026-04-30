package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.PlayerSettings;
import com.lkjmcsmp.persistence.PlayerSettingsDao;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlayerSettingsService {
    private final PlayerSettingsDao dao;
    private final Set<String> supportedLanguages;
    private final ConcurrentHashMap<UUID, PlayerSettings> cache = new ConcurrentHashMap<>();
    private Consumer<UUID> hotbarChangeHandler = playerId -> { };

    public PlayerSettingsService(PlayerSettingsDao dao, Set<String> supportedLanguages) {
        this.dao = dao;
        this.supportedLanguages = supportedLanguages;
    }

    public PlayerSettings get(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::load);
    }

    public String language(UUID playerId) {
        return get(playerId).language();
    }

    public boolean hotbarMenuEnabled(UUID playerId) {
        return get(playerId).hotbarMenuEnabled();
    }

    public PlayerSettings setLanguage(UUID playerId, String language) throws Exception {
        String normalized = normalizeLanguage(language);
        PlayerSettings current = get(playerId);
        PlayerSettings next = new PlayerSettings(normalized, current.hotbarMenuEnabled());
        persist(playerId, next);
        return next;
    }

    public PlayerSettings setHotbarMenuEnabled(UUID playerId, boolean enabled) throws Exception {
        PlayerSettings current = get(playerId);
        PlayerSettings next = new PlayerSettings(current.language(), enabled);
        persist(playerId, next);
        return next;
    }

    public PlayerSettings toggleHotbarMenu(UUID playerId) throws Exception {
        return setHotbarMenuEnabled(playerId, !hotbarMenuEnabled(playerId));
    }

    public void setHotbarChangeHandler(Consumer<UUID> handler) {
        this.hotbarChangeHandler = handler == null ? playerId -> { } : handler;
    }

    private PlayerSettings load(UUID playerId) {
        try {
            PlayerSettings loaded = dao.find(playerId).orElse(PlayerSettings.DEFAULT);
            if (!supportedLanguages.contains(loaded.language())) {
                return new PlayerSettings(PlayerSettings.DEFAULT_LANGUAGE, loaded.hotbarMenuEnabled());
            }
            return loaded;
        } catch (Exception ignored) {
            return PlayerSettings.DEFAULT;
        }
    }

    private String normalizeLanguage(String language) {
        String normalized = language == null ? "" : language.trim().toLowerCase();
        return supportedLanguages.contains(normalized) ? normalized : PlayerSettings.DEFAULT_LANGUAGE;
    }

    private void persist(UUID playerId, PlayerSettings settings) throws Exception {
        dao.upsert(playerId, settings);
        cache.put(playerId, settings);
        hotbarChangeHandler.accept(playerId);
    }
}
