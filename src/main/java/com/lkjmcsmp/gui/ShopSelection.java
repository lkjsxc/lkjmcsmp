package com.lkjmcsmp.gui;

record ShopSelection(String itemKey) {
    ShopSelection {
        itemKey = itemKey == null ? "" : itemKey.toLowerCase();
        if (itemKey.isBlank()) {
            throw new IllegalArgumentException("itemKey is required");
        }
    }
}
