package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import com.lkjmcsmp.persistence.EconomyOverrideDao;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

final class ShopCatalog {
    private ShopCatalog() {
    }

    static Map<String, ShopEntry> load(
            ConfigurationSection section,
            Iterable<EconomyOverrideDao.OverrideRecord> overrides) {
        Map<String, ShopEntry> items = parseItems(section);
        addBuiltInItems(items);
        items.putAll(HomeSlotCatalog.entries());
        mergeOverrides(items, overrides);
        return items;
    }

    private static Map<String, ShopEntry> parseItems(ConfigurationSection section) {
        Map<String, ShopEntry> items = new HashMap<>();
        if (section == null) {
            return items;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            Material material = Material.matchMaterial(entry.getString("material", ""));
            if (material == null) {
                continue;
            }
            String normalizedKey = key.toLowerCase();
            items.put(normalizedKey, new ShopEntry(
                    normalizedKey,
                    material,
                    entry.getString("display_name", key),
                    entry.getInt("points", 1),
                    entry.getBoolean("service", false),
                    entry.getString("environment", "")));
        }
        return items;
    }

    private static void addBuiltInItems(Map<String, ShopEntry> items) {
        items.putIfAbsent("end_stone", new ShopEntry(
                "end_stone",
                Material.END_STONE,
                "End Stone",
                8));
    }

    private static void mergeOverrides(
            Map<String, ShopEntry> baseItems,
            Iterable<EconomyOverrideDao.OverrideRecord> overrides) {
        for (EconomyOverrideDao.OverrideRecord override : overrides) {
            String itemKey = override.itemKey().toLowerCase();
            if (HomeSlotCatalog.isHomeSlotKey(itemKey)) {
                continue;
            }
            ShopEntry base = baseItems.get(itemKey);
            if (base == null) {
                continue;
            }
            baseItems.put(itemKey, new ShopEntry(
                    base.key(),
                    base.material(),
                    base.displayName(),
                    override.pointsCost(),
                    base.service(),
                    base.environment()));
        }
    }
}
