package com.lkjmcsmp.gui;

record ShopSelection(String itemKey, int units) {
    ShopSelection {
        itemKey = itemKey == null ? "" : itemKey.toLowerCase();
        if (itemKey.isBlank()) {
            throw new IllegalArgumentException("itemKey is required");
        }
        if (units < 1) {
            throw new IllegalArgumentException("units must be >= 1");
        }
    }

    ShopSelection withDelta(int delta) {
        int next = Math.max(1, Math.min(64, units + delta));
        return new ShopSelection(itemKey, next);
    }
}
