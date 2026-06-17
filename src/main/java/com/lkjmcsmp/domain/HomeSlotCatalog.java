package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.ShopEntry;
import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class HomeSlotCatalog {
    public static final String KEY_PREFIX = "home_slot_";
    private static final List<Integer> PRICES = List.of(
            2400, 3120, 4056, 5272, 6854, 8911, 11584,
            15059, 19577, 25450, 33086, 43011, 55915,
            72690, 94497, 122846, 159699, 207609, 269892,
            350860, 456119);
    private static final Map<String, ShopEntry> ENTRIES = buildEntries();

    private HomeSlotCatalog() {
    }

    public static List<Integer> prices() {
        return PRICES;
    }

    public static Map<String, ShopEntry> entries() {
        return ENTRIES;
    }

    public static int maxPurchasableSlots() {
        return PRICES.size();
    }

    public static String keyForSlotNumber(int slotNumber) {
        if (slotNumber < 1 || slotNumber > PRICES.size()) {
            throw new IllegalArgumentException("slot number out of range");
        }
        return KEY_PREFIX + "%02d".formatted(slotNumber);
    }

    public static boolean isHomeSlotKey(String key) {
        return slotNumber(key).isPresent();
    }

    public static OptionalInt expectedPurchasedSlots(String key) {
        OptionalInt slotNumber = slotNumber(key);
        return slotNumber.isPresent() ? OptionalInt.of(slotNumber.getAsInt() - 1) : OptionalInt.empty();
    }

    public static OptionalInt slotNumber(String key) {
        if (key == null) {
            return OptionalInt.empty();
        }
        String normalized = key.toLowerCase();
        if (!normalized.startsWith(KEY_PREFIX)) {
            return OptionalInt.empty();
        }
        String suffix = normalized.substring(KEY_PREFIX.length());
        if (suffix.length() != 2 || !suffix.chars().allMatch(Character::isDigit)) {
            return OptionalInt.empty();
        }
        int slotNumber = Integer.parseInt(suffix);
        return slotNumber >= 1 && slotNumber <= PRICES.size()
                ? OptionalInt.of(slotNumber)
                : OptionalInt.empty();
    }

    private static Map<String, ShopEntry> buildEntries() {
        Map<String, ShopEntry> entries = new LinkedHashMap<>();
        for (int i = 0; i < PRICES.size(); i++) {
            int slotNumber = i + 1;
            String key = keyForSlotNumber(slotNumber);
            entries.put(key, new ShopEntry(
                    key,
                    Material.RED_BED,
                    "Home Slot Upgrade " + "%02d".formatted(slotNumber),
                    PRICES.get(i),
                    true,
                    ""));
        }
        return Collections.unmodifiableMap(entries);
    }
}
