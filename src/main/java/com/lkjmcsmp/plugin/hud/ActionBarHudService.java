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
    private static final long SHOP_TTL_TICKS = 60L;
    private static final long TEMP_END_TTL_TICKS = 35L;
    private static final long IDLE_REFRESH_TICKS = 400L;
    private static final String IDLE_SOURCE = "idle";
    private static final String TELEPORT_SOURCE = "teleport";
    private static final String COMBAT_SOURCE = "combat";
    private static final String SHOP_SOURCE = "shop";
    private static final String TEMP_END_SOURCE = "tempend";
    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Map<UUID, PlayerHudState> states = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public ActionBarHudService(SchedulerBridge schedulerBridge, PointsService pointsService) {
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
    }

    public void start() {
        running = true;
        schedulerBridge.runGlobalDelayedTask(IDLE_REFRESH_TICKS, this::runIdleRefresh);
    }

    private void runIdleRefresh() {
        if (!running) {
            return;
        }
        refreshIdleAllOnline();
        schedulerBridge.runGlobalDelayedTask(IDLE_REFRESH_TICKS, this::runIdleRefresh);
    }

    public void stop() {
        running = false;
        states.clear();
    }

    public void onPlayerJoin(Player player) {
        if (player == null || !player.isOnline()) return;
        refreshIdle(player);
    }

    public void onPlayerQuit(Player player) {
        if (player == null) return;
        states.remove(player.getUniqueId());
        refreshIdleAllOnline();
    }

    public void refreshIdle(Player player) {
        if (player == null || !player.isOnline()) return;
        UUID playerId = player.getUniqueId();
        schedulerBridge.runAsyncTask(() -> {
            int points = 0;
            try { points = pointsService.getBalance(playerId); } catch (Exception ignored) { }
            int onlineCount = Bukkit.getOnlinePlayers().size();
            String text = "Maruishi Points: " + points + " | Online: " + onlineCount;
            states.computeIfAbsent(playerId, k -> new PlayerHudState()).put(
                    new ActionBarMessage(ActionBarPriority.IDLE, text, IDLE_SOURCE, -1));
            schedulerBridge.runPlayerTask(player, () -> renderCurrent(player));
        });
    }

    public void refreshIdleAllOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) refreshIdle(player);
    }

    public void onCombatHit(Player attacker, String targetName, double currentHealth, double maxHealth) {
        if (attacker == null || !attacker.isOnline()) return;
        String hpBar = buildHpBar(currentHealth, maxHealth);
        long expiresAt = System.currentTimeMillis() + (COMBAT_TTL_TICKS * 50L);
        setMessage(attacker, new ActionBarMessage(ActionBarPriority.COMBAT,
                "\u00A7e" + targetName + "\u00A7f " + hpBar, COMBAT_SOURCE, expiresAt));
        scheduleClear(attacker, COMBAT_SOURCE, COMBAT_TTL_TICKS);
    }

    @Override
    public void onTeleportCountdown(Player player, long secondsRemaining) {
        if (player == null || !player.isOnline()) return;
        long expiresAt = System.currentTimeMillis() + (TELEPORT_COUNTDOWN_TTL_TICKS * 50L);
        setMessage(player, new ActionBarMessage(ActionBarPriority.TELEPORT,
                "\u00A7bTeleport in \u00A7f" + secondsRemaining + "\u00A7bs", TELEPORT_SOURCE, expiresAt));
    }

    @Override
    public void onTeleportResult(Player player, boolean success, String message) {
        if (player == null || !player.isOnline()) return;
        String prefix = success ? "\u00A7aTeleport complete" : "\u00A7cTeleport failed";
        long expiresAt = System.currentTimeMillis() + (TELEPORT_RESULT_TTL_TICKS * 50L);
        setMessage(player, new ActionBarMessage(ActionBarPriority.TELEPORT,
                prefix + "\u00A77: \u00A7f" + message, TELEPORT_SOURCE, expiresAt));
        scheduleClear(player, TELEPORT_SOURCE, TELEPORT_RESULT_TTL_TICKS);
    }

    public void onShopPurchase(Player player, String itemKey, int cost) {
        if (player == null || !player.isOnline()) return;
        long expiresAt = System.currentTimeMillis() + (SHOP_TTL_TICKS * 50L);
        setMessage(player, new ActionBarMessage(ActionBarPriority.GAMEPLAY,
                "\u00A7aPurchased \u00A7f" + itemKey + "\u00A7a for \u00A7f" + cost + "\u00A7a Maruishi Points",
                SHOP_SOURCE, expiresAt));
        scheduleClear(player, SHOP_SOURCE, SHOP_TTL_TICKS);
    }

    public void onEnterTemporaryEnd(Player player, long remainingSeconds) {
        if (player == null || !player.isOnline()) return;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        long expiresAt = System.currentTimeMillis() + (TEMP_END_TTL_TICKS * 50L);
        setMessage(player, new ActionBarMessage(ActionBarPriority.GAMEPLAY,
                "\u00A75Temporary End \u00A7f" + minutes + "m " + seconds + "s remaining",
                TEMP_END_SOURCE, expiresAt));
    }

    public void onLeaveTemporaryEnd(Player player) {
        clearMessage(player, TEMP_END_SOURCE);
    }

    public void setMessage(Player player, ActionBarMessage message) {
        if (player == null || !player.isOnline()) return;
        states.computeIfAbsent(player.getUniqueId(), k -> new PlayerHudState()).put(message);
        renderCurrent(player);
    }

    public void clearMessage(Player player, String source) {
        if (player == null) return;
        PlayerHudState state = states.get(player.getUniqueId());
        if (state != null) {
            state.remove(source);
            if (!IDLE_SOURCE.equals(source)) state.clearLastSent();
        }
        if (player.isOnline()) renderCurrent(player);
    }

    private void scheduleClear(Player player, String source, long delayTicks) {
        schedulerBridge.runPlayerDelayedTask(player, delayTicks, () -> clearMessage(player, source));
    }

    private void renderCurrent(Player player) {
        if (player == null || !player.isOnline()) return;
        PlayerHudState state = states.get(player.getUniqueId());
        String effective = state != null ? state.computeEffective() : null;
        if (effective == null || effective.isEmpty()) {
            refreshIdle(player);
            return;
        }
        if (state != null && !state.shouldSend(effective)) return;
        final String text = effective;
        schedulerBridge.runPlayerTask(player, () -> { if (player.isOnline()) player.sendActionBar(text); });
    }

    private static String buildHpBar(double currentHealth, double maxHealth) {
        int segments = 16;
        if (maxHealth <= 0) return "\u00A77" + "|".repeat(segments);
        double ratio = Math.max(0.0D, Math.min(1.0D, currentHealth / maxHealth));
        int filled = (int) Math.round(ratio * segments);
        int empty = Math.max(0, segments - filled);
        return "\u00A7a" + "|".repeat(filled) + "\u00A77" + "|".repeat(empty);
    }
}
