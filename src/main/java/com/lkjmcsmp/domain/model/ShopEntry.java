package com.lkjmcsmp.domain.model;

import org.bukkit.Material;

public record ShopEntry(String key, Material material, String displayName, int points, boolean service, String environment) {
    public ShopEntry {
        service = service;
        environment = environment == null ? "" : environment;
    }

    public ShopEntry(String key, Material material, int points) {
        this(key, material, key, points, false, "");
    }

    public ShopEntry(String key, Material material, String displayName, int points) {
        this(key, material, displayName, points, false, "");
    }

    public ShopEntry(String key, Material material, String displayName, int points, boolean service) {
        this(key, material, displayName, points, service, "");
    }
}
