package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.PointsService;
import com.lkjmcsmp.plugin.scoreboard.SidebarRenderer;
import com.lkjmcsmp.plugin.scoreboard.SidebarSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SmpScoreboardService {
    private static final long RECONCILE_PERIOD_TICKS = 100L;
    private static final long JOIN_DELAY_TICKS = 20L;
    private static final long[] RETRY_DELAYS_TICKS = {20L, 100L, 200L};
    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Logger logger;
    private final SidebarRenderer sidebarRenderer = new SidebarRenderer();
    private final ConcurrentHashMap<UUID, Integer> renderEpochByPlayer = new ConcurrentHashMap<>();
    private final Set<UUID> trackedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> degradedPlayers = ConcurrentHashMap.newKeySet();
    private final AtomicInteger onlineCount = new AtomicInteger();
    private volatile boolean running;
    public SmpScoreboardService(SchedulerBridge schedulerBridge, PointsService pointsService, Logger logger) {
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
        this.logger = logger;
    }
    public void start() {
        stop();
        running = true;
        int initialOnlineCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            trackedPlayers.add(player.getUniqueId());
            initialOnlineCount++;
        }
        onlineCount.set(initialOnlineCount);
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayer(player, "startup", false);
        }
    }
    public void stop() {
        running = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                sidebarRenderer.clear(player);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Scoreboard clear failed (trigger=stop playerUuid=" + player.getUniqueId() + ")", ex);
            }
        }
        renderEpochByPlayer.clear();
        trackedPlayers.clear();
        degradedPlayers.clear();
        onlineCount.set(0);
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline() || !running) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (!trackedPlayers.contains(playerId)) {
            initializePlayer(player, "targeted", false);
            return;
        }
        dispatchSnapshot(player, "targeted", 1, renderEpochByPlayer.getOrDefault(playerId, 1), onlineCount.get(), false);
    }
    public void refreshOnJoin(Player player) {
        if (player == null || !running) {
            return;
        }
        initializePlayer(player, "join", true);
    }
    public void clear(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (trackedPlayers.remove(playerId)) {
            onlineCount.updateAndGet(current -> Math.max(0, current - 1));
        }
        renderEpochByPlayer.remove(playerId);
        degradedPlayers.remove(playerId);
        try {
            sidebarRenderer.clear(player);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Scoreboard clear failed (trigger=clear playerUuid=" + playerId + ")", ex);
        }
    }
    private void initializePlayer(Player player, String trigger, boolean delayedJoin) {
        UUID playerId = player.getUniqueId();
        if (trackedPlayers.add(playerId)) {
            onlineCount.incrementAndGet();
        }
        int epoch = nextEpoch(playerId);
        if (delayedJoin) {
            schedulerBridge.runPlayerDelayedTask(player, JOIN_DELAY_TICKS,
                    () -> dispatchSnapshotIfOnline(playerId, trigger, 1, epoch, false));
        } else {
            dispatchSnapshotIfOnline(playerId, trigger, 1, epoch, false);
        }
        schedulePeriodic(playerId, epoch);
    }
    private void schedulePeriodic(UUID playerId, int epoch) {
        if (!running || !isCurrent(playerId, epoch)) {
            return;
        }
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, RECONCILE_PERIOD_TICKS, () -> {
            if (!running || !isCurrent(playerId, epoch)) {
                return;
            }
            dispatchSnapshotIfOnline(playerId, "periodic", 1, epoch, false);
            schedulePeriodic(playerId, epoch);
        });
    }
    private void dispatchSnapshotIfOnline(UUID playerId, String trigger, int attempt, int epoch, boolean cleanupFirst) {
        if (!running || !isCurrent(playerId, epoch)) {
            return;
        }
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }
        dispatchSnapshot(player, trigger, attempt, epoch, onlineCount.get(), cleanupFirst);
    }
    private void dispatchSnapshot(Player player, String trigger, int attempt, int epoch, int currentOnlineCount, boolean cleanupFirst) {
        UUID playerId = player.getUniqueId();
        if (!running || !isCurrent(playerId, epoch)) {
            return;
        }
        schedulerBridge.runAsyncTask(() -> {
            int points = loadPoints(playerId, trigger, attempt);
            SidebarSnapshot snapshot = new SidebarSnapshot(trigger, attempt, epoch, currentOnlineCount, points, cleanupFirst);
            schedulerBridge.runPlayerTask(player,
                    () -> renderSnapshot(player, snapshot));
        });
    }
    private int loadPoints(UUID playerId, String trigger, int attempt) {
        try {
            return pointsService.getBalance(playerId);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Scoreboard points lookup failed (" + context(trigger, playerId, attempt)
                    + ") fallbackPoints=0", ex);
            return 0;
        }
    }
    private void renderSnapshot(Player player, SidebarSnapshot snapshot) {
        UUID playerId = player.getUniqueId();
        if (!running || !player.isOnline() || !isCurrent(playerId, snapshot.epoch())) {
            return;
        }
        try {
            sidebarRenderer.render(player, snapshot);
            if (degradedPlayers.remove(playerId)) {
                logger.info("Scoreboard recovered (" + context(snapshot.trigger(), playerId, snapshot.attempt()) + ")");
            }
        } catch (Exception ex) {
            handleRenderFailure(player, snapshot, ex);
        }
    }
    private void handleRenderFailure(Player player, SidebarSnapshot snapshot, Exception failure) {
        UUID playerId = player.getUniqueId();
        if (!running || !isCurrent(playerId, snapshot.epoch())) {
            return;
        }
        logger.log(Level.WARNING, "Scoreboard render failed ("
                + context(snapshot.trigger(), playerId, snapshot.attempt()) + ")", failure);
        int retryIndex = snapshot.attempt() - 1;
        if (retryIndex >= RETRY_DELAYS_TICKS.length) {
            degradedPlayers.add(playerId);
            logger.severe("Scoreboard retries exhausted ("
                    + context(snapshot.trigger(), playerId, snapshot.attempt()) + ")");
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, RETRY_DELAYS_TICKS[retryIndex],
                () -> dispatchSnapshotIfOnline(
                        playerId,
                        snapshot.trigger(),
                        snapshot.attempt() + 1,
                        snapshot.epoch(),
                        true));
    }
    private int nextEpoch(UUID playerId) { return renderEpochByPlayer.merge(playerId, 1, Integer::sum); }
    private boolean isCurrent(UUID playerId, int epoch) { return renderEpochByPlayer.getOrDefault(playerId, -1) == epoch; }
    private static String context(String trigger, UUID playerId, int attempt) { return "trigger=" + trigger + " playerUuid=" + playerId + " attempt=" + attempt; }
}
