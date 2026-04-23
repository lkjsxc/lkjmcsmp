package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.plugin.SchedulerBridge;

import java.time.Instant;
import java.util.logging.Logger;

public final class TemporaryDimensionLifecycle {
    private static final long CHECK_INTERVAL_TICKS = 6000L;
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionManager manager;
    private final Logger logger;

    public TemporaryDimensionLifecycle(SchedulerBridge schedulerBridge, TemporaryDimensionManager manager, Logger logger) {
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
            logger.warning("Temporary dimension lifecycle check failed: " + e.getMessage());
        }
        scheduleNext();
    }
}
