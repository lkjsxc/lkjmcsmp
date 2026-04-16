package com.lkjmcsmp.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.logging.Logger;

public final class TeleportCommandOverrideListener implements Listener {
    private final Logger logger;

    public TeleportCommandOverrideListener(Logger logger) {
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || !message.startsWith("/")) {
            return;
        }
        String[] parts = message.substring(1).split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        if (!command.equals("tp")) {
            return;
        }
        event.setCancelled(true);
        String dispatch = "lkjmcsmp:tp" + (parts.length > 1 ? " " + parts[1] : "");
        boolean dispatched = event.getPlayer().performCommand(dispatch);
        if (!dispatched) {
            event.getPlayer().sendMessage("Teleport override failed. Use /lkjmcsmp:tp <player>.");
            logger.warning("Teleport override dispatch failed for " + event.getPlayer().getName() + ": " + dispatch);
        }
    }
}
