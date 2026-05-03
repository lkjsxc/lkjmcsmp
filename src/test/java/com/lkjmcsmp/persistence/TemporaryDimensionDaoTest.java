package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ParticipantLifecycle;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryDimensionDaoTest {
    @TempDir
    Path tempDir;

    @Test
    void participantLifecycleSupportsDeferredReturnRetry() throws Exception {
        TemporaryDimensionDao dao = dao();
        String instanceId = "inst-" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        dao.insertInstance(instance(instanceId));
        dao.insertParticipant(instanceId, playerId, ParticipantLifecycle.PENDING_TRANSFER, location("world"));

        assertEquals(1, dao.countParticipantsByState(instanceId).get(ParticipantLifecycle.PENDING_TRANSFER));
        dao.updateParticipantState(instanceId, playerId, ParticipantLifecycle.ACTIVE);
        assertEquals(1, dao.listParticipants(instanceId, ParticipantLifecycle.ACTIVE).size());

        dao.updateState(instanceId, InstanceLifecycle.CLOSED);
        assertTrue(dao.findPendingReturns(playerId).isEmpty());
        dao.markActiveParticipantsReturnPending(instanceId);
        var pending = dao.findPendingReturns(playerId);
        assertEquals(1, pending.size());
        assertEquals("world", pending.get(0).location().world());

        dao.deleteParticipant(instanceId, playerId);
        dao.deleteClosedInstanceIfNoParticipants(instanceId);
        assertTrue(dao.listByState(InstanceLifecycle.CLOSED).isEmpty());
    }

    private TemporaryDimensionDao dao() throws Exception {
        SqliteDatabase database = new SqliteDatabase(tempDir.resolve(UUID.randomUUID() + ".db"));
        database.initialize();
        return new TemporaryDimensionDao(database);
    }

    private static TemporaryDimensionInstance instance(String instanceId) {
        Instant now = Instant.now();
        return new TemporaryDimensionInstance(
                instanceId,
                "world_" + instanceId,
                UUID.randomUUID(),
                World.Environment.THE_END,
                location("origin"),
                now,
                now.plusSeconds(60),
                InstanceLifecycle.ACTIVE);
    }

    private static NamedLocation location(String world) {
        return new NamedLocation("", world, 1, 2, 3, 4, 5);
    }
}
