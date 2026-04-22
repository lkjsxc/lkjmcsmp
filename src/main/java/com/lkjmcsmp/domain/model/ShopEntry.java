package com.lkjmcsmp.domain.model;

import org.bukkit.Material;

public record ShopEntry(String key, Material material, int points, boolean service) {
    public ShopEntry {
        service = service;
    }

    public ShopEntry(String key, Material material, int points) {
        this(key, material, points, false);
    }
}
