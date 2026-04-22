package com.lkjmcsmp.plugin.temporaryend;

import com.lkjmcsmp.plugin.SchedulerBridge;

import java.time.Instant;
import java.util.logging.Logger;

public final class TemporaryEndLifecycle {
    private static final long CHECK_INTERVAL_TICKS = 6000L;
    private final SchedulerBridge schedulerBridge;
    private final TemporaryEndManager manager;
    private final Logger logger;

    public TemporaryEndLifecycle(SchedulerBridge schedulerBridge, TemporaryEndManager manager, Logger logger) {
        this.schedulerBridge = schedulerBridge;
        this.manager = manager;
        this.logger = logger;
    }

    public void start() {
        scheduleNext();
    }

    private void scheduleNext() {
        schedulerBridge.runGlobalDelayedTask(CHECK_INTERVAL_TICKS, this::runCheck);
    }

    private void runCheck() {
        try {
            for (var instance : manager.activeInstances()) {
                if (instance.expirationTime().isBefore(Instant.now())) {
                    manager.expireInstance(instance.instanceId());
                }
            }
        } catch (Exception e) {
            logger.warning("Temporary end lifecycle check failed: " + e.getMessage());
        }
        scheduleNext();
    }
}
