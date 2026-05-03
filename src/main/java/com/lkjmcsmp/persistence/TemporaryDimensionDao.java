package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ParticipantLifecycle;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TemporaryDimensionDao {
    private final SqliteDatabase database;
    private final TemporaryDimensionParticipants participants;

    public TemporaryDimensionDao(SqliteDatabase database) {
        this.database = database;
        this.participants = new TemporaryDimensionParticipants(database);
    }

    public void insertInstance(TemporaryDimensionInstance instance) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_dimension_instances
                     (instance_id, world_name, creator_uuid, environment, origin_world, origin_x, origin_y, origin_z,
                      origin_yaw, origin_pitch, creation_time, expiration_time, state)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, instance.instanceId());
            statement.setString(2, instance.worldName());
            statement.setString(3, instance.creatorUuid().toString());
            statement.setString(4, instance.environment().name());
            TemporaryDimensionMapper.bindLocation(statement, 5, instance.origin());
            statement.setString(11, instance.creationTime().toString());
            statement.setString(12, instance.expirationTime().toString());
            statement.setString(13, instance.state().name());
            statement.executeUpdate();
        }
    }

    public void updateState(String instanceId, InstanceLifecycle state) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE temporary_dimension_instances SET state = ? WHERE instance_id = ?")) {
            statement.setString(1, state.name());
            statement.setString(2, instanceId);
            statement.executeUpdate();
        }
    }

    public void deleteInstance(String instanceId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_dimension_instances WHERE instance_id = ?")) {
            statement.setString(1, instanceId);
            statement.executeUpdate();
        }
    }

    public List<TemporaryDimensionInstance> listByState(InstanceLifecycle state) throws Exception {
        List<TemporaryDimensionInstance> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM temporary_dimension_instances WHERE state = ?")) {
            statement.setString(1, state.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) results.add(TemporaryDimensionMapper.instance(rs));
            }
        }
        return results;
    }

    public void insertParticipant(
            String instanceId,
            UUID playerUuid,
            ParticipantLifecycle state,
            NamedLocation returnLoc) throws Exception {
        participants.insert(instanceId, playerUuid, state, returnLoc);
    }

    public void updateParticipantState(String instanceId, UUID playerUuid, ParticipantLifecycle state) throws Exception {
        participants.updateState(instanceId, playerUuid, state);
    }

    public void markActiveParticipantsReturnPending(String instanceId) throws Exception {
        participants.markActiveReturnPending(instanceId);
    }

    public void deleteParticipantsByInstance(String instanceId) throws Exception {
        participants.deleteByInstance(instanceId);
    }

    public List<ParticipantRecord> listParticipants(String instanceId) throws Exception {
        return participants.list(instanceId, null);
    }

    public List<ParticipantRecord> listParticipants(String instanceId, ParticipantLifecycle state) throws Exception {
        return participants.list(instanceId, state);
    }

    public List<ParticipantReturn> findPendingReturns(UUID playerUuid) throws Exception {
        return participants.findPendingReturns(playerUuid);
    }

    public Optional<NamedLocation> findParticipantReturn(UUID playerUuid) throws Exception {
        return participants.findActiveReturn(playerUuid);
    }

    public void deleteParticipant(String instanceId, UUID playerUuid) throws Exception {
        participants.delete(instanceId, playerUuid);
    }

    public Map<ParticipantLifecycle, Integer> countParticipantsByState(String instanceId) throws Exception {
        return participants.countByState(instanceId);
    }

    public void deleteClosedInstanceIfNoParticipants(String instanceId) throws Exception {
        try (var connection = database.open()) {
            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM temporary_dimension_participants WHERE instance_id = ?")) {
                count.setString(1, instanceId);
                try (ResultSet rs = count.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return;
                }
            }
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM temporary_dimension_instances WHERE instance_id = ? AND state = 'CLOSED'")) {
                delete.setString(1, instanceId);
                delete.executeUpdate();
            }
        }
    }

    public record ParticipantReturn(String instanceId, NamedLocation location) { }
    public record ParticipantRecord(UUID playerUuid, ParticipantLifecycle state, NamedLocation location) { }
}
