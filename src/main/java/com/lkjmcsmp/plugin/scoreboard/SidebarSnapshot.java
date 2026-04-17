package com.lkjmcsmp.plugin.scoreboard;

public record SidebarSnapshot(
        String trigger,
        int attempt,
        int epoch,
        int onlineCount,
        int points,
        boolean cleanupFirst) {
}
