package com.lkjmcsmp.plugin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public final class SidebarRenderer {
    private static final String OBJECTIVE_NAME = "lkjmcsmp";
    private static final String TITLE = "lkjmcsmp SMP";
    private static final String ONLINE_TEAM = "lkj_online";
    private static final String POINTS_TEAM = "lkj_points";
    private static final String ONLINE_ENTRY = "\u00A70";
    private static final String POINTS_ENTRY = "\u00A71";
    private static final int ONLINE_LINE_SCORE = 2;
    private static final int POINTS_LINE_SCORE = 1;

    public void render(Player player, SidebarSnapshot snapshot) {
        ScoreboardManager manager = scoreboardManager();
        if (snapshot.cleanupFirst()) {
            player.setScoreboard(manager.getMainScoreboard());
        }
        player.setScoreboard(createManagedBoard(snapshot));
    }

    public void clear(Player player) {
        player.setScoreboard(scoreboardManager().getMainScoreboard());
    }

    public Scoreboard createManagedBoard(SidebarSnapshot snapshot) {
        String onlineLine = "Online: " + snapshot.onlineCount();
        String pointsLine = "Points: " + snapshot.points();
        Scoreboard board = scoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, TITLE);
        bindLine(board, objective, ONLINE_TEAM, ONLINE_ENTRY, onlineLine, ONLINE_LINE_SCORE);
        bindLine(board, objective, POINTS_TEAM, POINTS_ENTRY, pointsLine, POINTS_LINE_SCORE);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        verify(board, objective, onlineLine, pointsLine);
        return board;
    }

    private static void bindLine(
            Scoreboard board,
            Objective objective,
            String teamName,
            String entry,
            String lineText,
            int score) {
        Team team = board.registerNewTeam(teamName);
        team.addEntry(entry);
        team.setPrefix(lineText);
        objective.getScore(entry).setScore(score);
    }

    private static void verify(Scoreboard board, Objective objective, String onlineLine, String pointsLine) {
        if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR || board.getObjective(DisplaySlot.SIDEBAR) != objective) {
            throw new IllegalStateException("sidebar display slot missing");
        }
        if (!objective.getScore(ONLINE_ENTRY).isScoreSet() || !objective.getScore(POINTS_ENTRY).isScoreSet()) {
            throw new IllegalStateException("required entries missing");
        }
        if (objective.getScore(ONLINE_ENTRY).getScore() != ONLINE_LINE_SCORE
                || objective.getScore(POINTS_ENTRY).getScore() != POINTS_LINE_SCORE) {
            throw new IllegalStateException("line ordering mismatch");
        }
        Team onlineTeam = board.getTeam(ONLINE_TEAM);
        Team pointsTeam = board.getTeam(POINTS_TEAM);
        if (onlineTeam == null || pointsTeam == null) {
            throw new IllegalStateException("required teams missing");
        }
        String actualOnline = onlineTeam.getPrefix() == null ? "" : onlineTeam.getPrefix();
        String actualPoints = pointsTeam.getPrefix() == null ? "" : pointsTeam.getPrefix();
        if (!onlineLine.equals(actualOnline) || !pointsLine.equals(actualPoints)) {
            throw new IllegalStateException("required lines missing");
        }
    }

    private static ScoreboardManager scoreboardManager() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            throw new IllegalStateException("scoreboard manager unavailable");
        }
        return manager;
    }
}
