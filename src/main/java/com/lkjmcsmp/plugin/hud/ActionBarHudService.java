package com.lkjmcsmp.plugin.hud;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportHudSink;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionBarHudService implements TeleportHudSink {
    private static final long COMBAT_TTL_TICKS = 60L;
    private static final long TELEPORT_RESULT_TTL_TICKS = 40L;
    private static final long TELEPORT_COUNTDOWN_TTL_TICKS = 25L;
    private static final String NO_DATA_IDLE = "Points: 0 | Online: 0";
    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Map<UUID, String> idleTextByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, TimedMessage> teleportMessages = new ConcurrentHashMap<>();
    private final Map<UUID, TimedMessage> combatMessages = new ConcurrentHashMap<>();

    public ActionBarHudService(SchedulerBridge schedulerBridge, PointsService pointsService) {
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
    }

    public void stop() {
        idleTextByPlayer.clear();
        teleportMessages.clear();
        combatMessages.clear();
    }

    public void onPlayerJoin(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        refreshIdleAllOnline();
    }

    public void onPlayerQuit(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        idleTextByPlayer.remove(playerId);
        teleportMessages.remove(playerId);
        combatMessages.remove(playerId);
        refreshIdleAllOnline();
    }

    public void refreshIdle(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        schedulerBridge.runAsyncTask(() -> {
            int points = 0;
            try {
                points = pointsService.getBalance(playerId);
            } catch (Exception ignored) {
            }
            int onlineCount = Bukkit.getOnlinePlayers().size();
            idleTextByPlayer.put(playerId, "Points: " + points + " | Online: " + onlineCount);
            schedulerBridge.runPlayerTask(player, () -> renderCurrent(player));
        });
    }

    public void refreshIdleAllOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshIdle(player);
        }
    }

    public void onCombatHit(Player attacker, String targetName, double currentHealth, double maxHealth) {
        if (attacker == null || !attacker.isOnline()) {
            return;
        }
        UUID playerId = attacker.getUniqueId();
        String hpBar = buildHpBar(currentHealth, maxHealth);
        combatMessages.put(playerId, TimedMessage.of("\u00A7e" + targetName + " \u00A7fHP " + hpBar, COMBAT_TTL_TICKS));
        renderCurrent(attacker);
        scheduleRender(attacker, COMBAT_TTL_TICKS);
    }

    @Override
    public void onTeleportCountdown(Player player, long secondsRemaining) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        teleportMessages.put(playerId, TimedMessage.of(
                "\u00A7bTeleport in \u00A7f" + secondsRemaining + "\u00A7bs",
                TELEPORT_COUNTDOWN_TTL_TICKS));
        renderCurrent(player);
    }

    @Override
    public void onTeleportResult(Player player, boolean success, String message) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        String prefix = success ? "\u00A7aTeleport complete" : "\u00A7cTeleport failed";
        teleportMessages.put(playerId, TimedMessage.of(prefix + "\u00A77: \u00A7f" + message, TELEPORT_RESULT_TTL_TICKS));
        renderCurrent(player);
        scheduleRender(player, TELEPORT_RESULT_TTL_TICKS);
    }

    private void scheduleRender(Player player, long delayTicks) {
        schedulerBridge.runPlayerDelayedTask(player, delayTicks, () -> renderCurrent(player));
    }

    private void renderCurrent(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        TimedMessage teleport = activeMessage(teleportMessages, playerId);
        if (teleport != null) {
            player.sendActionBar(teleport.text());
            return;
        }
        TimedMessage combat = activeMessage(combatMessages, playerId);
        if (combat != null) {
            player.sendActionBar(combat.text());
            return;
        }
        player.sendActionBar(idleTextByPlayer.getOrDefault(playerId, NO_DATA_IDLE));
    }

    private static TimedMessage activeMessage(Map<UUID, TimedMessage> messages, UUID playerId) {
        TimedMessage current = messages.get(playerId);
        if (current == null) {
            return null;
        }
        if (current.expiresAtMs() < System.currentTimeMillis()) {
            messages.remove(playerId);
            return null;
        }
        return current;
    }

    private static String buildHpBar(double currentHealth, double maxHealth) {
        int segments = 16;
        if (maxHealth <= 0) {
            return "\u00A77" + "|".repeat(segments);
        }
        double ratio = Math.max(0.0D, Math.min(1.0D, currentHealth / maxHealth));
        int filled = (int) Math.round(ratio * segments);
        int empty = Math.max(0, segments - filled);
        return "\u00A7a" + "|".repeat(filled) + "\u00A77" + "|".repeat(empty);
    }

    private record TimedMessage(String text, long expiresAtMs) {
        private static TimedMessage of(String text, long ttlTicks) {
            return new TimedMessage(text, System.currentTimeMillis() + (ttlTicks * 50L));
        }
    }
}
