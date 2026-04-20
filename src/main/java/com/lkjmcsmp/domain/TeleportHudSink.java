package com.lkjmcsmp.domain;

import org.bukkit.entity.Player;

public interface TeleportHudSink {
    TeleportHudSink NO_OP = new TeleportHudSink() {
    };

    default void onTeleportCountdown(Player player, long secondsRemaining) {
    }

    default void onTeleportResult(Player player, boolean success, String message) {
    }
}
