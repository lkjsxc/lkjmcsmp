package com.lkjmcsmp.domain;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopCatalogTest {
    @Test
    void defaultShopConfigIncludesEndStone() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/shop.yml"));

        var items = ShopCatalog.load(config.getConfigurationSection("items"), List.of());

        assertEndStone(items);
        assertEquals(8, items.get("oak_log").points());
        assertEquals(8, items.get("spruce_log").points());
        assertEquals(8, items.get("birch_log").points());
        assertTrue(items.keySet().stream().noneMatch(HomeSlotCatalog::isHomeSlotKey));
    }

    @Test
    void builtInEndStoneRemainsAvailableWhenShopConfigIsMissingIt() {
        var items = ShopCatalog.load(new YamlConfiguration().createSection("items"), List.of());

        assertEndStone(items);
    }

    private static void assertEndStone(Map<String, com.lkjmcsmp.domain.model.ShopEntry> items) {
        assertTrue(items.containsKey("end_stone"));
        assertEquals(Material.END_STONE, items.get("end_stone").material());
        assertEquals(8, items.get("end_stone").points());
        assertEquals("End Stone", items.get("end_stone").displayName());
    }
}
