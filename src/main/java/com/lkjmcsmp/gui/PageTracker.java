package com.lkjmcsmp.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PageTracker {
    private final Map<UUID, Map<String, Integer>> pagesByPlayer = new ConcurrentHashMap<>();

    int page(UUID playerId, String title) {
        return pagesByPlayer.getOrDefault(playerId, Map.of()).getOrDefault(title, 0);
    }

    void setPage(UUID playerId, String title, int page) {
        pagesByPlayer.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .put(title, Math.max(0, page));
    }

    void clear(UUID playerId) {
        pagesByPlayer.remove(playerId);
    }
}
