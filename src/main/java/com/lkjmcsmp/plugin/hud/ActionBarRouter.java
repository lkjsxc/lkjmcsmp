package com.lkjmcsmp.plugin.hud;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.domain.TeleportHudSink;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ActionBarRouter implements TeleportHudSink {
    private static final long TICK_INTERVAL = 2L;
    private static final long COMBAT_TTL_MS = 3000L;
    private static final long TELEPORT_RESULT_TTL_MS = 2000L;
    private static final long SHOP_TTL_MS = 3000L;
    private static final long TEMP_DIM_TTL_MS = 30000L;
    private static final String IDLE_SOURCE = "idle";
    private static final String TELEPORT_SOURCE = "teleport";
    private static final String COMBAT_SOURCE = "combat";
    private static final String SHOP_SOURCE = "shop";
    private static final String TEMP_DIM_SOURCE = "tempdim";

    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Map<UUID, PlayerHudState> states = new ConcurrentHashMap<>();
    private final ActionBarRenderer renderer;
    private final AtomicInteger onlineCount = new AtomicInteger(0);
    private volatile boolean running = false;

    public ActionBarRouter(SchedulerBridge schedulerBridge, PointsService pointsService) {
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
        this.renderer = new ActionBarRenderer(schedulerBridge);
    }

    public void start() {
        running = true;
        onlineCount.set(Bukkit.getOnlinePlayers().size());
        for (Player player : Bukkit.getOnlinePlayers()) {
            onPlayerJoin(player);
        }
    }

    public void stop() {
        running = false;
        states.clear();
    }

    public void onPlayerJoin(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        states.computeIfAbsent(player.getUniqueId(), k -> new PlayerHudState());
        refreshIdle(player);
        schedulerBridge.runPlayerTask(player, () -> scheduleNext(player));
    }

    public void onPlayerQuit(Player player) {
        if (player == null) {
            return;
        }
        states.remove(player.getUniqueId());
    }

    public void incrementOnlineCount() {
        onlineCount.incrementAndGet();
    }

    public void decrementOnlineCount() {
        onlineCount.decrementAndGet();
    }

    public void refreshIdle(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        int count = onlineCount.get();
        schedulerBridge.runAsyncTask(() -> {
            int points = 0;
            try {
                points = pointsService.getBalance(playerId);
            } catch (Exception ignored) {
            }
            String text = ActionBarComposer.idle(points, count);
            states.computeIfAbsent(playerId, k -> new PlayerHudState()).put(
                    new ActionBarMessage(ActionBarPriority.IDLE, text, IDLE_SOURCE, -1));
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
        long expiresAt = System.currentTimeMillis() + COMBAT_TTL_MS;
        setMessage(attacker, new ActionBarMessage(ActionBarPriority.COMBAT,
                ActionBarComposer.combat(targetName, currentHealth, maxHealth), COMBAT_SOURCE, expiresAt));
    }

    @Override
    public void onTeleportCountdown(Player player, long secondsRemaining) {
        if (player == null || !player.isOnline()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + 1250L;
        setMessage(player, new ActionBarMessage(ActionBarPriority.TELEPORT,
                ActionBarComposer.teleportCountdown(secondsRemaining), TELEPORT_SOURCE, expiresAt));
    }

    @Override
    public void onTeleportResult(Player player, boolean success, String message) {
        if (player == null || !player.isOnline()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + TELEPORT_RESULT_TTL_MS;
        setMessage(player, new ActionBarMessage(ActionBarPriority.TELEPORT,
                ActionBarComposer.teleportResult(success, message), TELEPORT_SOURCE, expiresAt));
    }

    public void onShopPurchase(Player player, String itemKey, int cost) {
        if (player == null || !player.isOnline()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + SHOP_TTL_MS;
        setMessage(player, new ActionBarMessage(ActionBarPriority.GAMEPLAY,
                ActionBarComposer.shopPurchase(itemKey, cost), SHOP_SOURCE, expiresAt));
    }

    public void onEnterTemporaryDimension(Player player, long remainingSeconds) {
        if (player == null || !player.isOnline()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + TEMP_DIM_TTL_MS;
        setMessage(player, new ActionBarMessage(ActionBarPriority.GAMEPLAY,
                ActionBarComposer.temporaryDimension(remainingSeconds), TEMP_DIM_SOURCE, expiresAt));
    }

    public void onLeaveTemporaryDimension(Player player) {
        clearMessage(player, TEMP_DIM_SOURCE);
    }

    public void onEnterTemporaryEnd(Player player, long remainingSeconds) {
        onEnterTemporaryDimension(player, remainingSeconds);
    }

    public void onLeaveTemporaryEnd(Player player) {
        onLeaveTemporaryDimension(player);
    }

    public void setMessage(Player player, ActionBarMessage message) {
        if (player == null || !player.isOnline()) {
            return;
        }
        states.computeIfAbsent(player.getUniqueId(), k -> new PlayerHudState()).put(message);
    }

    public void clearMessage(Player player, String source) {
        if (player == null) {
            return;
        }
        PlayerHudState state = states.get(player.getUniqueId());
        if (state != null) {
            state.remove(source);
            if (!IDLE_SOURCE.equals(source)) {
                state.clearLastSent();
            }
        }
    }

    private void scheduleNext(Player player) {
        if (!running || !player.isOnline()) {
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, TICK_INTERVAL, () -> {
            if (!running || !player.isOnline()) {
                return;
            }
            PlayerHudState state = states.get(player.getUniqueId());
            if (state != null) {
                renderer.render(player, state);
            }
            scheduleNext(player);
        });
    }
}
