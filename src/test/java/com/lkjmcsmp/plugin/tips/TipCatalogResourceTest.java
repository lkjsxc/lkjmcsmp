package com.lkjmcsmp.plugin.tips;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TipCatalogResourceTest {
    @Test
    void bundledTipCatalogHasSharedStructuredCategories() {
        List<String> categories = load("src/main/resources/tips/catalog.yml").getStringList("categories");

        assertEquals(10, categories.size());
        assertEquals(200, countTips("en", categories));
        assertEquals(200, countTips("ja", categories));
    }

    @Test
    void categoryFilesContainTwentyNonBlankTipsWithoutOldHomeVerbs() {
        List<String> categories = load("src/main/resources/tips/catalog.yml").getStringList("categories");

        for (String language : List.of("en", "ja")) {
            for (String category : categories) {
                List<String> tips = load(path(language, category)).getStringList("tips");
                assertEquals(20, tips.size(), language + "/" + category);
                assertTrue(tips.stream().noneMatch(String::isBlank), language + "/" + category);
                assertTrue(tips.stream().noneMatch(tip ->
                        tip.contains("add" + "current") || tip.contains("set" + "home")));
            }
        }
    }

    private static int countTips(String language, List<String> categories) {
        return categories.stream().mapToInt(category -> load(path(language, category)).getStringList("tips").size()).sum();
    }

    private static String path(String language, String category) {
        return "src/main/resources/tips/" + language + "/" + category + ".yml";
    }

    private static YamlConfiguration load(String path) {
        File file = new File(path);
        assertTrue(file.exists(), path);
        return YamlConfiguration.loadConfiguration(file);
    }
}
