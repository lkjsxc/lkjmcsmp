package com.lkjmcsmp.plugin.hud;

import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class ActionBarIdleRefresh {
    private static final String IDLE_SOURCE = "idle";

    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final MessageService messages;
    private final Map<UUID, Integer> cachedPoints = new ConcurrentHashMap<>();

    ActionBarIdleRefresh(SchedulerBridge schedulerBridge, PointsService pointsService, MessageService messages) {
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
        this.messages = messages;
    }

    void clear(Player player) {
        if (player != null) {
            cachedPoints.remove(player.getUniqueId());
        }
    }

    void refreshAsync(Player player, int onlineCount, Map<UUID, PlayerHudState> states) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        schedulerBridge.runAsyncTask(() -> {
            int points = cachedPoints.getOrDefault(playerId, 0);
            try {
                points = pointsService.getBalance(playerId);
            } catch (Exception ignored) {
            }
            cachedPoints.put(playerId, points);
            schedulerBridge.runPlayerTask(player, () -> {
                if (player.isOnline()) {
                    refreshNow(player, onlineCount, states);
                }
            });
        });
    }

    void refreshNow(Player player, int onlineCount, Map<UUID, PlayerHudState> states) {
        long playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int points = cachedPoints.getOrDefault(player.getUniqueId(), 0);
        String text = ActionBarComposer.idle(points, playtimeTicks, onlineCount, messages.get(player, "hud.idle"));
        states.computeIfAbsent(player.getUniqueId(), k -> new PlayerHudState()).put(
                new ActionBarMessage(ActionBarPriority.IDLE, text, IDLE_SOURCE, -1));
    }
}
