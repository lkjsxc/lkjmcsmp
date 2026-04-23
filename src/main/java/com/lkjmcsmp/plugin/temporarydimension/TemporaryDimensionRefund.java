package com.lkjmcsmp.plugin.temporarydimension;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.persistence.PointsDao;
import com.lkjmcsmp.persistence.TemporaryDimensionDao;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.entity.Player;
import java.util.logging.Logger;

final class TemporaryDimensionRefund {
    private final SchedulerBridge schedulerBridge;
    private final TemporaryDimensionDao temporaryDimensionDao;
    private final PointsDao pointsDao;
    private final Logger logger;
    private final int cost;

    TemporaryDimensionRefund(SchedulerBridge schedulerBridge, TemporaryDimensionDao temporaryDimensionDao,
                             PointsDao pointsDao, Logger logger, int cost) {
        this.schedulerBridge = schedulerBridge;
        this.temporaryDimensionDao = temporaryDimensionDao;
        this.pointsDao = pointsDao;
        this.logger = logger;
        this.cost = cost;
    }

    void refundAndNotify(Player player, String reason) {
        try {
            pointsDao.addPoints(player.getUniqueId(), cost, "TEMPORARY_DIMENSION_REFUND", "{\"reason\":\"" + reason + "\"}");
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed. \u00A7a" + cost + " Cobblestone Points refunded."));
        } catch (Exception ex) {
            logger.severe("Refund failed for " + player.getUniqueId() + ": " + ex.getMessage());
            schedulerBridge.runPlayerTask(player, () -> player.sendMessage("\u00A7cCreation failed and refund could not be applied. Contact an admin."));
        }
    }

    void cleanupDb(String instanceId, boolean deleted) {
        try {
            temporaryDimensionDao.updateState(instanceId, InstanceLifecycle.CLOSED);
            if (deleted) {
                temporaryDimensionDao.deleteParticipantsByInstance(instanceId);
                temporaryDimensionDao.deleteInstance(instanceId);
            }
            logger.info("Cleaned up temporary dimension instance " + instanceId);
        } catch (Exception e) {
            logger.warning("Cleanup DB update failed for " + instanceId + ": " + e.getMessage());
        }
    }
}
