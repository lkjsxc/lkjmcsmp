package com.lkjmcsmp.gui;

record ShopSelection(String itemKey, int quantity) {
    ShopSelection {
        itemKey = itemKey == null ? "" : itemKey.toLowerCase();
        if (itemKey.isBlank()) {
            throw new IllegalArgumentException("itemKey is required");
        }
        if (quantity < 1 || quantity > 64) {
            throw new IllegalArgumentException("quantity must be in 1..64");
        }
    }

    ShopSelection withDelta(int delta) {
        int next = Math.max(1, Math.min(64, quantity + delta));
        return new ShopSelection(itemKey, next);
    }

    ShopSelection withQuantity(int nextQuantity) {
        int bounded = Math.max(1, Math.min(64, nextQuantity));
        return new ShopSelection(itemKey, bounded);
    }
}
