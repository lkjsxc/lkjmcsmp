package com.lkjmcsmp.domain;

import com.lkjmcsmp.persistence.EconomyOverrideDao;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HomeSlotCatalogTest {
    @Test
    void exposesFixedHomeSlotCatalogInKeyOrder() {
        assertEquals(List.of(
                2400, 3120, 4056, 5272, 6854, 8911, 11584,
                15059, 19577, 25450, 33086, 43011, 55915,
                72690, 94497, 122846, 159699, 207609, 269892,
                350860, 456119),
                HomeSlotCatalog.prices());
        assertEquals(21, HomeSlotCatalog.entries().size());
        assertEquals("home_slot_01", HomeSlotCatalog.keyForSlotNumber(1));
        assertEquals("home_slot_21", HomeSlotCatalog.keyForSlotNumber(21));
        assertEquals("home_slot_01", List.copyOf(HomeSlotCatalog.entries().keySet()).get(0));
        assertEquals("home_slot_21", List.copyOf(HomeSlotCatalog.entries().keySet()).get(20));
        assertEquals(2400, HomeSlotCatalog.entries().get("home_slot_01").points());
        assertEquals(Material.RED_BED, HomeSlotCatalog.entries().get("home_slot_01").material());
        assertTrue(HomeSlotCatalog.entries().get("home_slot_01").service());
    }

    @Test
    void recognizesOnlyCanonicalHomeSlotKeys() {
        assertTrue(HomeSlotCatalog.isHomeSlotKey("home_slot_01"));
        assertTrue(HomeSlotCatalog.isHomeSlotKey("HOME_SLOT_21"));
        assertEquals(0, HomeSlotCatalog.expectedPurchasedSlots("home_slot_01").orElseThrow());
        assertEquals(20, HomeSlotCatalog.expectedPurchasedSlots("home_slot_21").orElseThrow());
        assertFalse(HomeSlotCatalog.isHomeSlotKey("home_slot_00"));
        assertFalse(HomeSlotCatalog.isHomeSlotKey("home_slot_22"));
        assertFalse(HomeSlotCatalog.isHomeSlotKey("home_slot_1"));
        assertFalse(HomeSlotCatalog.isHomeSlotKey("temporary_dimension_pass"));
    }

    @Test
    void builtInHomeSlotsOverrideConfigAndIgnoreSeasonalOverrides() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.home_slot_01.material", "DIRT");
        config.set("items.home_slot_01.points", 1);
        config.set("items.home_slot_01.display_name", "Bad Runtime Config");

        var items = ShopCatalog.load(
                config.getConfigurationSection("items"),
                List.of(new EconomyOverrideDao.OverrideRecord("home_slot_01", 1, 1)));

        assertEquals(2400, items.get("home_slot_01").points());
        assertEquals(Material.RED_BED, items.get("home_slot_01").material());
        assertEquals("Home Slot Upgrade 01", items.get("home_slot_01").displayName());
    }
}
