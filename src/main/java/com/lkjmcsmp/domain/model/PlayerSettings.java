package com.lkjmcsmp.domain.model;

public record PlayerSettings(String language, boolean hotbarMenuEnabled) {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final PlayerSettings DEFAULT = new PlayerSettings(DEFAULT_LANGUAGE, true);

    public PlayerSettings {
        language = language == null || language.isBlank() ? DEFAULT_LANGUAGE : language.toLowerCase();
    }
}
