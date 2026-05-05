package com.lkjmcsmp.plugin.hud;

import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.entity.Player;

final class ActionBarRenderer {
    private final SchedulerBridge schedulerBridge;

    ActionBarRenderer(SchedulerBridge schedulerBridge) {
        this.schedulerBridge = schedulerBridge;
    }

    void render(Player player, PlayerHudState state) {
        if (player == null || !player.isOnline()) {
            return;
        }
        String effective = state != null ? state.computeEffective() : null;
        if (effective == null || effective.isEmpty()) {
            effective = ActionBarComposer.idle(0L, 0);
        }
        final String text = effective;
        schedulerBridge.runPlayerTask(player, () -> {
            if (player.isOnline()) {
                player.sendActionBar(text);
            }
        });
    }
}
