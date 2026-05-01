package com.lkjmcsmp.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkjmcsmp.domain.model.PlayerSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MessageService {
    private final Map<String, Map<String, String>> catalogs;
    private final PlayerSettingsService settings;
    private final LanguageRegistry languages;

    public MessageService(JavaPlugin plugin, PlayerSettingsService settings) {
        this(plugin, settings, loadRegistry(plugin));
    }

    public MessageService(JavaPlugin plugin, PlayerSettingsService settings, LanguageRegistry languages) {
        this.settings = settings;
        this.languages = languages;
        this.catalogs = loadCatalogs(plugin, languages);
    }

    public Set<String> supportedLanguages() {
        return catalogs.keySet();
    }

    public LanguageRegistry languages() {
        return languages;
    }

    public String get(Player player, String key, Object... placeholders) {
        return get(settings.language(player.getUniqueId()), key, placeholders);
    }

    public String get(String language, String key, Object... placeholders) {
        String normalized = catalogs.containsKey(language) ? language : languages.defaultLanguage();
        String text = catalogs.getOrDefault(normalized, Map.of()).get(key);
        if (text == null) {
            text = catalogs.getOrDefault(languages.defaultLanguage(), Map.of()).getOrDefault(key, key);
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

    private static Map<String, Map<String, String>> loadCatalogs(JavaPlugin plugin, LanguageRegistry registry) {
        Map<String, Map<String, String>> loaded = new LinkedHashMap<>();
        for (String code : registry.languages().keySet()) {
            loaded.put(code, load(plugin, "lang/" + code + ".json"));
        }
        loaded.putIfAbsent(PlayerSettings.DEFAULT_LANGUAGE, load(plugin, "lang/en.json"));
        return Map.copyOf(loaded);
    }

    public static LanguageRegistry loadRegistry(JavaPlugin plugin) {
        try (var stream = plugin.getResource("lang/languages.yml")) {
            if (stream == null) return fallbackRegistry();
            var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String fallback = yaml.getString("default", PlayerSettings.DEFAULT_LANGUAGE).toLowerCase();
            Map<String, String> names = new LinkedHashMap<>();
            var section = yaml.getConfigurationSection("languages");
            if (section != null) {
                for (String code : section.getKeys(false)) {
                    names.put(code.toLowerCase(), section.getString(code + ".display", code));
                }
            }
            if (names.isEmpty()) return fallbackRegistry();
            if (!names.containsKey(fallback)) fallback = names.keySet().iterator().next();
            return new LanguageRegistry(fallback, Map.copyOf(names));
        } catch (Exception ignored) {
            return fallbackRegistry();
        }
    }

    private static LanguageRegistry fallbackRegistry() {
        return new LanguageRegistry(PlayerSettings.DEFAULT_LANGUAGE, Map.of("en", "English", "ja", "日本語"));
    }

    public record LanguageRegistry(String defaultLanguage, Map<String, String> languages) {
        public Set<String> codes() { return languages.keySet(); }
    }
}
