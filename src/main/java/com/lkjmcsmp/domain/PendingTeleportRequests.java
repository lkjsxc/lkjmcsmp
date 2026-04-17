package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.TpaRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PendingTeleportRequests {
    private final Map<UUID, LinkedHashMap<UUID, TpaRequest>> byTarget = new ConcurrentHashMap<>();

    synchronized void put(TpaRequest request) {
        purgeExpired(request.to());
        LinkedHashMap<UUID, TpaRequest> targetRequests = byTarget.computeIfAbsent(request.to(), unused -> new LinkedHashMap<>());
        targetRequests.remove(request.from());
        targetRequests.put(request.from(), request);
    }

    synchronized List<TpaRequest> list(UUID targetId) {
        purgeExpired(targetId);
        LinkedHashMap<UUID, TpaRequest> targetRequests = byTarget.get(targetId);
        if (targetRequests == null || targetRequests.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(targetRequests.values());
    }

    synchronized Optional<TpaRequest> remove(UUID targetId, UUID requesterId) {
        purgeExpired(targetId);
        LinkedHashMap<UUID, TpaRequest> targetRequests = byTarget.get(targetId);
        if (targetRequests == null || targetRequests.isEmpty()) {
            return Optional.empty();
        }
        TpaRequest removed;
        if (requesterId == null) {
            UUID firstRequester = targetRequests.keySet().iterator().next();
            removed = targetRequests.remove(firstRequester);
        } else {
            removed = targetRequests.remove(requesterId);
        }
        if (targetRequests.isEmpty()) {
            byTarget.remove(targetId);
        }
        return Optional.ofNullable(removed);
    }

    private void purgeExpired(UUID targetId) {
        LinkedHashMap<UUID, TpaRequest> targetRequests = byTarget.get(targetId);
        if (targetRequests == null || targetRequests.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        targetRequests.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        if (targetRequests.isEmpty()) {
            byTarget.remove(targetId);
        }
    }
}
