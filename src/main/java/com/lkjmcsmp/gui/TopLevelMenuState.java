package com.lkjmcsmp.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class TopLevelMenuState {
    private final Map<UUID, ShopSelection> shopSelections = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> shopPages = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> achievementPages = new ConcurrentHashMap<>();

    void resetShopSelection(UUID playerId) {
        shopSelections.remove(playerId);
    }

    void clear(UUID playerId) {
        shopSelections.remove(playerId);
        shopPages.remove(playerId);
        achievementPages.remove(playerId);
    }

    ShopSelection shopSelection(UUID playerId) {
        return shopSelections.get(playerId);
    }

    void setShopSelection(UUID playerId, ShopSelection selection) {
        shopSelections.put(playerId, selection);
    }

    int shopPage(UUID playerId) {
        return shopPages.getOrDefault(playerId, 0);
    }

    int achievementPage(UUID playerId) {
        return achievementPages.getOrDefault(playerId, 0);
    }

    void setShopPage(UUID playerId, int page) {
        shopPages.put(playerId, Math.max(0, page));
    }

    void setAchievementPage(UUID playerId, int page) {
        achievementPages.put(playerId, Math.max(0, page));
    }
}
