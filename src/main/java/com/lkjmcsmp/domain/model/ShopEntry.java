package com.lkjmcsmp.domain.model;

import org.bukkit.Material;

public record ShopEntry(String key, Material material, int quantity, int points) {
}
