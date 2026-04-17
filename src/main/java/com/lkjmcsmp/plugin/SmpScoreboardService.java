package com.lkjmcsmp.plugin;
import com.lkjmcsmp.domain.PointsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
public final class SmpScoreboardService {
    private static final String OBJECTIVE_NAME = "lkjmcsmp";
    private static final String TITLE = "lkjmcsmp SMP";
    private static final int ONLINE_LINE_SCORE = 2;
    private static final int POINTS_LINE_SCORE = 1;
    private static final long RECONCILE_PERIOD_TICKS = 100L;
    private static final long JOIN_DELAY_TICKS = 20L;
    private static final long[] RETRY_DELAYS_TICKS = {20L, 100L, 200L};
    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Logger logger;
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
        int initialOnline = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            trackedPlayers.add(player.getUniqueId());
            initialOnline++;
        }
        onlineCount.set(initialOnline);
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializePlayer(player, "startup", false);
        }
    }
    public void stop() {
        running = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }
        renderEpochByPlayer.clear();
        trackedPlayers.clear();
        degradedPlayers.clear();
        onlineCount.set(0);
    }
    public void refresh(Player player) {
        if (player == null || !player.isOnline() || !running) return;
        UUID playerId = player.getUniqueId();
        if (!trackedPlayers.contains(playerId)) {
            initializePlayer(player, "targeted", false);
            return;
        }
        dispatchSnapshot(player, "targeted", 1, renderEpochByPlayer.getOrDefault(playerId, 1), onlineCount.get(), false);
    }

    public void refreshOnJoin(Player player) {
        if (player == null || !running) return;
        initializePlayer(player, "join", true);
    }

    public void clear(Player player) {
        if (player == null) return;
        UUID playerId = player.getUniqueId();
        if (trackedPlayers.remove(playerId)) onlineCount.updateAndGet(current -> Math.max(0, current - 1));
        renderEpochByPlayer.remove(playerId);
        degradedPlayers.remove(playerId);
        if (Bukkit.getScoreboardManager() == null) {
            logger.warning("Scoreboard clear skipped (trigger=clear playerUuid=" + playerId + "): manager unavailable");
            return;
        }
        try {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Scoreboard clear failed (trigger=clear playerUuid=" + playerId + ")", ex);
        }
    }

    private void initializePlayer(Player player, String trigger, boolean delayedJoin) {
        UUID playerId = player.getUniqueId();
        if (trackedPlayers.add(playerId)) onlineCount.incrementAndGet();
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
        if (!running || !isCurrent(playerId, epoch)) return;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;
        schedulerBridge.runPlayerDelayedTask(player, RECONCILE_PERIOD_TICKS, () -> {
            if (!running || !isCurrent(playerId, epoch)) return;
            dispatchSnapshotIfOnline(playerId, "periodic", 1, epoch, false);
            schedulePeriodic(playerId, epoch);
        });
    }

    private void dispatchSnapshotIfOnline(UUID playerId, String trigger, int attempt, int epoch, boolean cleanupFirst) {
        if (!running || !isCurrent(playerId, epoch)) return;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;
        dispatchSnapshot(player, trigger, attempt, epoch, onlineCount.get(), cleanupFirst);
    }

    private void dispatchSnapshot(Player player, String trigger, int attempt, int epoch, int currentOnlineCount, boolean cleanupFirst) {
        UUID playerId = player.getUniqueId();
        if (!running || !isCurrent(playerId, epoch)) return;
        schedulerBridge.runAsyncTask(() -> {
            int points = loadPoints(playerId, trigger, attempt);
            schedulerBridge.runPlayerTask(player,
                    () -> render(player, trigger, attempt, epoch, currentOnlineCount, points, cleanupFirst));
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

    private void render(Player player, String trigger, int attempt, int epoch, int currentOnlineCount, int points, boolean cleanupFirst) {
        UUID playerId = player.getUniqueId();
        if (!running || !player.isOnline() || !isCurrent(playerId, epoch)) return;
        if (Bukkit.getScoreboardManager() == null) {
            handleRenderFailure(player, trigger, attempt, epoch, new IllegalStateException("scoreboard manager unavailable"));
            return;
        }
        try {
            if (cleanupFirst) player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            String onlineLine = "Online: " + currentOnlineCount;
            String pointsLine = "Points: " + points;
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, TITLE);
            objective.getScore(onlineLine).setScore(ONLINE_LINE_SCORE);
            objective.getScore(pointsLine).setScore(POINTS_LINE_SCORE);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            player.setScoreboard(board);
            verifySidebar(player, onlineLine, pointsLine);
            if (degradedPlayers.remove(playerId)) logger.info("Scoreboard recovered (" + context(trigger, playerId, attempt) + ")");
        } catch (Exception ex) {
            handleRenderFailure(player, trigger, attempt, epoch, ex);
        }
    }

    private void verifySidebar(Player player, String onlineLine, String pointsLine) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective == null) throw new IllegalStateException("managed objective missing");
        if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR || board.getObjective(DisplaySlot.SIDEBAR) != objective) {
            throw new IllegalStateException("sidebar display slot missing");
        }
        if (!objective.getScore(onlineLine).isScoreSet() || !objective.getScore(pointsLine).isScoreSet()) {
            throw new IllegalStateException("required lines missing");
        }
        if (objective.getScore(onlineLine).getScore() != ONLINE_LINE_SCORE || objective.getScore(pointsLine).getScore() != POINTS_LINE_SCORE) {
            throw new IllegalStateException("line ordering mismatch");
        }
    }

    private void handleRenderFailure(Player player, String trigger, int attempt, int epoch, Exception failure) {
        UUID playerId = player.getUniqueId();
        if (!running || !isCurrent(playerId, epoch)) return;
        logger.log(Level.WARNING, "Scoreboard render failed (" + context(trigger, playerId, attempt) + ")", failure);
        int retryIndex = attempt - 1;
        if (retryIndex >= RETRY_DELAYS_TICKS.length) {
            degradedPlayers.add(playerId);
            logger.severe("Scoreboard retries exhausted (" + context(trigger, playerId, attempt) + ")");
            return;
        }
        schedulerBridge.runPlayerDelayedTask(player, RETRY_DELAYS_TICKS[retryIndex],
                () -> dispatchSnapshotIfOnline(playerId, trigger, attempt + 1, epoch, true));
    }

    private int nextEpoch(UUID playerId) { return renderEpochByPlayer.merge(playerId, 1, Integer::sum); }
    private boolean isCurrent(UUID playerId, int epoch) { return renderEpochByPlayer.getOrDefault(playerId, -1) == epoch; }
    private static String context(String trigger, UUID playerId, int attempt) { return "trigger=" + trigger + " playerUuid=" + playerId + " attempt=" + attempt; }
}
