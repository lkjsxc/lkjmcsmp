package com.lkjmcsmp.plugin.tips;

import com.lkjmcsmp.domain.MessageService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TipCatalog {
    private final String fallbackLanguage;
    private final Map<String, List<String>> tipsByLanguage;

    private TipCatalog(String fallbackLanguage, Map<String, List<String>> tipsByLanguage) {
        this.fallbackLanguage = fallbackLanguage;
        this.tipsByLanguage = tipsByLanguage;
    }

    public static TipCatalog load(JavaPlugin plugin, MessageService.LanguageRegistry languages) {
        List<String> categories = loadCategories(plugin);
        Map<String, List<String>> tips = new LinkedHashMap<>();
        for (String code : languages.languages().keySet()) {
            tips.put(code, loadLanguage(plugin, code, categories));
        }
        tips.putIfAbsent(languages.defaultLanguage(), loadLanguage(plugin, languages.defaultLanguage(), categories));
        return new TipCatalog(languages.defaultLanguage(), Map.copyOf(tips));
    }

    public Optional<String> tip(String language, int index) {
        List<String> tips = tipsByLanguage.getOrDefault(language, tipsByLanguage.get(fallbackLanguage));
        if (tips == null || tips.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tips.get(Math.floorMod(index, tips.size())));
    }

    public int size(String language) {
        List<String> tips = tipsByLanguage.getOrDefault(language, tipsByLanguage.get(fallbackLanguage));
        return tips == null ? 0 : tips.size();
    }

    public boolean empty() {
        return size(fallbackLanguage) == 0;
    }

    private static List<String> loadCategories(JavaPlugin plugin) {
        try (var stream = plugin.getResource("tips/catalog.yml")) {
            if (stream == null) {
                return List.of();
            }
            var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return yaml.getStringList("categories");
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static List<String> loadLanguage(JavaPlugin plugin, String language, List<String> categories) {
        List<String> tips = new ArrayList<>();
        for (String category : categories) {
            tips.addAll(loadTips(plugin, "tips/" + language + "/" + category + ".yml"));
        }
        return List.copyOf(tips);
    }

    private static List<String> loadTips(JavaPlugin plugin, String path) {
        try (var stream = plugin.getResource(path)) {
            if (stream == null) {
                return List.of();
            }
            var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return yaml.getStringList("tips");
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
