package com.lkjmcsmp.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkjmcsmp.domain.model.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public final class MessageService {
    private final Map<String, Map<String, String>> catalogs;
    private final PlayerSettingsService settings;

    public MessageService(JavaPlugin plugin, PlayerSettingsService settings) {
        this.settings = settings;
        this.catalogs = Map.of(
                "en", load(plugin, "lang/en.json"),
                "ja", load(plugin, "lang/ja.json"));
    }

    public Set<String> supportedLanguages() {
        return catalogs.keySet();
    }

    public String get(Player player, String key, Object... placeholders) {
        return get(settings.language(player.getUniqueId()), key, placeholders);
    }

    public String get(String language, String key, Object... placeholders) {
        String normalized = catalogs.containsKey(language) ? language : PlayerSettings.DEFAULT_LANGUAGE;
        String text = catalogs.getOrDefault(normalized, Map.of()).get(key);
        if (text == null) {
            text = catalogs.get(PlayerSettings.DEFAULT_LANGUAGE).getOrDefault(key, key);
        }
        return apply(text, placeholders);
    }

    private static String apply(String text, Object... placeholders) {
        String result = text;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            result = result.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return result;
    }

    private static Map<String, String> load(JavaPlugin plugin, String path) {
        try (var stream = plugin.getResource(path)) {
            if (stream == null) {
                return Map.of();
            }
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() { }.getType());
            }
        } catch (Exception ignored) {
            return Map.of();
        }
    }
}
