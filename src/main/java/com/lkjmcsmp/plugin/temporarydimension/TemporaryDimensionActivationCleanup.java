package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;

import java.util.Map;
import java.util.logging.Logger;

final class TemporaryDimensionActivationCleanup {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao dao;
    private final TemporaryDimensionWorldFactory worldFactory;
    private final Logger logger;

    TemporaryDimensionActivationCleanup(
            SchedulerBridge schedulerBridge,
            TemporaryDimensionDao dao,
            TemporaryDimensionWorldFactory worldFactory,
            Logger logger) {
        this.schedulerBridge = schedulerBridge;
        this.dao = dao;
        this.worldFactory = worldFactory;
        this.logger = logger;
    }

    void cleanup(String instanceId, String worldName, Map<String, ?> activeInstances) {
        activeInstances.remove(instanceId);
        schedulerBridge.runAsyncTask(() -> {
            try {
                dao.deleteParticipantsByInstance(instanceId);
                dao.deleteInstance(instanceId);
            } catch (Exception e) {
                logger.warning("Failed to cleanup failed temporary dimension DB records: " + e.getMessage());
            }
        });
        schedulerBridge.runGlobalTask(() -> worldFactory.unloadAndDelete(worldName));
    }
}
