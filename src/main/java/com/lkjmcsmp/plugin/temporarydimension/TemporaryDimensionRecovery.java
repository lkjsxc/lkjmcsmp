package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.logging.Logger;

final class TemporaryDimensionRecovery {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao dao;
    private final Map<String, TemporaryDimensionInstance> activeInstances;
    private final Logger logger;

    TemporaryDimensionRecovery(
            SchedulerBridge schedulerBridge,
            TemporaryDimensionDao dao,
            Map<String, TemporaryDimensionInstance> activeInstances,
            Logger logger) {
        this.schedulerBridge = schedulerBridge;
        this.dao = dao;
        this.activeInstances = activeInstances;
        this.logger = logger;
    }

    void recoverOnStartup() {
        schedulerBridge.runAsyncTask(() -> {
            try {
                var active = dao.listByState(InstanceLifecycle.ACTIVE);
                schedulerBridge.runGlobalTask(() -> reconcile(active));
            } catch (Exception e) {
                logger.warning("Temporary dimension startup recovery failed: " + e.getMessage());
            }
        });
    }

    private void reconcile(Iterable<TemporaryDimensionInstance> active) {
        for (TemporaryDimensionInstance instance : active) {
            if (Bukkit.getWorld(instance.worldName()) != null) {
                activeInstances.put(instance.instanceId(), instance);
                logger.info("Recovered temporary dimension instance: " + instance.instanceId());
            } else {
                closeMissing(instance);
            }
        }
    }

    private void closeMissing(TemporaryDimensionInstance instance) {
        try {
            dao.updateState(instance.instanceId(), InstanceLifecycle.CLOSED);
            dao.markActiveParticipantsReturnPending(instance.instanceId());
            dao.deleteClosedInstanceIfNoParticipants(instance.instanceId());
            logger.info("Closed missing temporary dimension world: " + instance.instanceId());
        } catch (Exception e) {
            logger.warning("Cleanup failed for orphaned record: " + e.getMessage());
        }
    }
}
