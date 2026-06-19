package com.lkjmcsmp.domain.model;

public record PlayerSettings(
        String language,
        boolean hotbarMenuEnabled,
        boolean actionBarEnabled,
        boolean tipsEnabled) {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final PlayerSettings DEFAULT = new PlayerSettings(DEFAULT_LANGUAGE, true, true, true);

    public PlayerSettings(String language, boolean hotbarMenuEnabled) {
        this(language, hotbarMenuEnabled, true, true);
    }

    public PlayerSettings(String language, boolean hotbarMenuEnabled, boolean actionBarEnabled) {
        this(language, hotbarMenuEnabled, actionBarEnabled, true);
    }

    public PlayerSettings {
        language = language == null || language.isBlank() ? DEFAULT_LANGUAGE : language.toLowerCase();
    }
}
