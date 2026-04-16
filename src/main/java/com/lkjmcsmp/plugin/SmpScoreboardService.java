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

import java.util.UUID;
import java.util.logging.Logger;

public final class SmpScoreboardService {
    private static final String OBJECTIVE_NAME = "lkjmcsmp";
    private static final String TITLE = "lkjmcsmp SMP";
    private final JavaPlugin plugin;
    private final PointsService pointsService;
    private final Logger logger;
    private ScheduledTask refreshTask;

    public SmpScoreboardService(JavaPlugin plugin, PointsService pointsService, Logger logger) {
        this.plugin = plugin;
        this.pointsService = pointsService;
        this.logger = logger;
    }

    public void start() {
        stop();
        refreshTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> refreshAll(),
                20L,
                100L);
    }

    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    public void refresh(Player player) {
        refreshPlayer(player, Bukkit.getOnlinePlayers().size());
    }

    public void clear(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void refreshAll() {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player, onlineCount);
        }
    }

    private void refreshPlayer(Player player, int onlineCount) {
        UUID playerId = player.getUniqueId();
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            int points;
            try {
                points = pointsService.getBalance(playerId);
            } catch (Exception ex) {
                logger.warning("Failed to read points for scoreboard " + player.getName() + ": " + ex.getMessage());
                points = 0;
            }
            int resolvedPoints = points;
            player.getScheduler().run(plugin, scheduledTask -> render(player, onlineCount, resolvedPoints), null);
        });
    }

    private static void render(Player player, int onlineCount, int points) {
        if (!player.isOnline()) {
            return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, TITLE);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("Online: " + onlineCount).setScore(2);
        objective.getScore("Points: " + points).setScore(1);
        player.setScoreboard(scoreboard);
    }
}
