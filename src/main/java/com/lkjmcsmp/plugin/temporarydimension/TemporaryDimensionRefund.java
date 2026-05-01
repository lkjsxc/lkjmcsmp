package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import java.util.logging.Logger;

final class TemporaryDimensionRefund {
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final Logger logger;

    TemporaryDimensionRefund(TemporaryDimensionDao temporaryDimensionDao, Logger logger) {
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.logger = logger;
    }

    void cleanupDb(String instanceId, boolean deleted) {
        try {
            temporaryDimensionDao.updateState(instanceId, InstanceLifecycle.CLOSED);
            if (deleted) {
                temporaryDimensionDao.deleteClosedInstanceIfNoParticipants(instanceId);
            }
            logger.info("Cleaned up temporary dimension instance " + instanceId);
        } catch (Exception e) {
            logger.warning("Cleanup DB update failed for " + instanceId + ": " + e.getMessage());
        }
    }
}
