package com.lkjmcsmp.plugin.tips;

import com.lkjmcsmp.domain.MessageService;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicLong;

public final class TipService {
    public static final long DEFAULT_INTERVAL_TICKS = 72000L;
    private final SchedulerBridge schedulerBridge;
    private final MessageService messages;
    private final TipCatalog catalog;
    private final boolean enabled;
    private final long intervalTicks;
    private final AtomicLong counter = new AtomicLong();
    private volatile boolean running;

    public TipService(
            SchedulerBridge schedulerBridge,
            MessageService messages,
            TipCatalog catalog,
            boolean enabled,
            long intervalTicks) {
        this.schedulerBridge = schedulerBridge;
        this.messages = messages;
        this.catalog = catalog;
        this.enabled = enabled;
        this.intervalTicks = Math.max(20L, intervalTicks);
    }

    public void start() {
        if (!enabled || catalog.empty()) {
            return;
        }
        running = true;
        scheduleNext();
    }

    public void stop() {
        running = false;
    }

    private void scheduleNext() {
        schedulerBridge.runGlobalDelayedTask(intervalTicks, this::sendAndSchedule);
    }

    private void sendAndSchedule() {
        if (!running) {
            return;
        }
        long sequence = counter.getAndIncrement();
        for (Player player : Bukkit.getOnlinePlayers()) {
            schedulerBridge.runPlayerTask(player, () -> sendTip(player, sequence));
        }
        scheduleNext();
    }

    private void sendTip(Player player, long sequence) {
        if (!running || player == null || !player.isOnline()) {
            return;
        }
        String language = messages.language(player);
        int size = Math.max(1, catalog.size(language));
        int offset = Math.floorMod(player.getUniqueId().hashCode(), size);
        catalog.tip(language, (int) (sequence + offset))
                .ifPresent(tip -> player.sendMessage(messages.get(player, "tips.prefix") + " " + tip));
    }
}
