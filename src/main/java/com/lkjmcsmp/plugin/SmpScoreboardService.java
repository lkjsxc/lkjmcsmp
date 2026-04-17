package com.lkjmcsmp.plugin;
import com.lkjmcsmp.domain.PointsService;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
public final class SmpScoreboardService {
    private static final String OBJECTIVE_NAME = "lkjmcsmp";
    private static final String TITLE = "lkjmcsmp SMP";
    private static final int ONLINE_LINE_SCORE = 2;
    private static final int POINTS_LINE_SCORE = 1;
    private static final long RECONCILE_PERIOD_TICKS = 100L;
    private static final long[] RETRY_DELAYS_TICKS = {20L, 100L, 200L};

    private final JavaPlugin plugin;
    private final SchedulerBridge schedulerBridge;
    private final PointsService pointsService;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, Integer> renderEpochByPlayer = new ConcurrentHashMap<>();
    private final Set<UUID> degradedPlayers = ConcurrentHashMap.newKeySet();
    private ScheduledTask refreshTask;

    public SmpScoreboardService(JavaPlugin plugin, SchedulerBridge schedulerBridge, PointsService pointsService, Logger logger) {
        this.plugin = plugin;
        this.schedulerBridge = schedulerBridge;
        this.pointsService = pointsService;
        this.logger = logger;
    }

    public void start() {
        stop();
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> reconcileOnline("startup"));
        refreshTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,
                task -> reconcileOnline("periodic"), RECONCILE_PERIOD_TICKS, RECONCILE_PERIOD_TICKS);
    }

    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }
        renderEpochByPlayer.clear();
        degradedPlayers.clear();
    }

    public void refresh(Player player) {
        if (player != null) requestRefresh(player.getUniqueId(), "targeted");
    }

    public void refreshOnJoin(Player player) {
        if (player != null) requestRefresh(player.getUniqueId(), "join");
    }

    public void clear(Player player) {
        if (player == null) return;
        UUID playerId = player.getUniqueId();
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

    private void requestRefresh(UUID playerId, String trigger) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) return;
            int epoch = nextEpoch(playerId);
            dispatchSnapshot(player, trigger, 1, epoch, Bukkit.getOnlinePlayers().size(), false);
        });
    }

    private void reconcileOnline(String trigger) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            dispatchSnapshot(player, trigger, 1, nextEpoch(player.getUniqueId()), onlineCount, false);
        }
    }

    private void dispatchSnapshot(Player player, String trigger, int attempt, int epoch, int onlineCount, boolean cleanupFirst) {
        UUID playerId = player.getUniqueId();
        if (!isCurrent(playerId, epoch)) return;
        schedulerBridge.runAsyncTask(() -> {
            int points = loadPoints(playerId, trigger, attempt);
            schedulerBridge.runPlayerTask(player, () -> render(player, trigger, attempt, epoch, onlineCount, points, cleanupFirst));
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

    private void render(Player player, String trigger, int attempt, int epoch, int onlineCount, int points, boolean cleanupFirst) {
        UUID playerId = player.getUniqueId();
        if (!player.isOnline() || !isCurrent(playerId, epoch)) return;
        if (Bukkit.getScoreboardManager() == null) {
            handleRenderFailure(player, trigger, attempt, epoch, new IllegalStateException("scoreboard manager unavailable"));
            return;
        }
        try {
            if (cleanupFirst) player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            String onlineLine = "Online: " + onlineCount;
            String pointsLine = "Points: " + points;
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, TITLE);
            objective.getScore(onlineLine).setScore(ONLINE_LINE_SCORE);
            objective.getScore(pointsLine).setScore(POINTS_LINE_SCORE);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            verifySidebar(board, objective, onlineLine, pointsLine);
            player.setScoreboard(board);
            if (degradedPlayers.remove(playerId)) logger.info("Scoreboard recovered (" + context(trigger, playerId, attempt) + ")");
        } catch (Exception ex) {
            handleRenderFailure(player, trigger, attempt, epoch, ex);
        }
    }

    private void verifySidebar(Scoreboard board, Objective objective, String onlineLine, String pointsLine) {
        if (board.getObjective(OBJECTIVE_NAME) == null) throw new IllegalStateException("managed objective missing");
        if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR || board.getObjective(DisplaySlot.SIDEBAR) != objective) {
            throw new IllegalStateException("sidebar display slot missing");
        }
        if (!board.getEntries().contains(onlineLine) || !board.getEntries().contains(pointsLine)) {
            throw new IllegalStateException("required lines missing");
        }
        if (objective.getScore(onlineLine).getScore() != ONLINE_LINE_SCORE
                || objective.getScore(pointsLine).getScore() != POINTS_LINE_SCORE) {
            throw new IllegalStateException("line ordering mismatch");
        }
    }

    private void handleRenderFailure(Player player, String trigger, int attempt, int epoch, Exception failure) {
        UUID playerId = player.getUniqueId();
        if (!isCurrent(playerId, epoch)) return;
        logger.log(Level.WARNING, "Scoreboard render failed (" + context(trigger, playerId, attempt) + ")", failure);
        int retryIndex = attempt - 1;
        if (retryIndex >= RETRY_DELAYS_TICKS.length) {
            degradedPlayers.add(playerId);
            logger.severe("Scoreboard retries exhausted (" + context(trigger, playerId, attempt) + ")");
            return;
        }
        try {
            schedulerBridge.runPlayerDelayedTask(player, RETRY_DELAYS_TICKS[retryIndex],
                    () -> scheduleRetry(playerId, trigger, attempt + 1, epoch));
        } catch (Exception delayedFailure) {
            degradedPlayers.add(playerId);
            logger.log(Level.SEVERE, "Scoreboard retry scheduling failed (" + context(trigger, playerId, attempt) + ")", delayedFailure);
        }
    }

    private void scheduleRetry(UUID playerId, String trigger, int attempt, int epoch) {
        if (!isCurrent(playerId, epoch)) return;
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            if (!isCurrent(playerId, epoch)) return;
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) return;
            dispatchSnapshot(player, trigger, attempt, epoch, Bukkit.getOnlinePlayers().size(), true);
        });
    }

    private int nextEpoch(UUID playerId) { return renderEpochByPlayer.merge(playerId, 1, Integer::sum); }
    private boolean isCurrent(UUID playerId, int epoch) { return renderEpochByPlayer.getOrDefault(playerId, -1) == epoch; }

    private static String context(String trigger, UUID playerId, int attempt) {
        return "trigger=" + trigger + " playerUuid=" + playerId + " attempt=" + attempt;
    }
}
