package com.lkjmcsmp.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SmpScoreboardListener implements Listener {
    private final SmpScoreboardService scoreboardService;

    public SmpScoreboardListener(SmpScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scoreboardService.refreshOnJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        scoreboardService.clear(event.getPlayer());
    }
}
