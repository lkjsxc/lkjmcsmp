package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TemporaryDimensionDao {
    private final SqliteDatabase database;

    public TemporaryDimensionDao(SqliteDatabase database) {
        this.database = database;
    }

    public void insertInstance(TemporaryDimensionInstance instance) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_dimension_instances
                     (instance_id, world_name, creator_uuid, environment, origin_world, origin_x, origin_y, origin_z, origin_yaw, origin_pitch, creation_time, expiration_time, state)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, instance.instanceId());
            statement.setString(2, instance.worldName());
            statement.setString(3, instance.creatorUuid().toString());
            statement.setString(4, instance.environment().name());
            statement.setString(5, instance.origin().world());
            statement.setDouble(6, instance.origin().x());
            statement.setDouble(7, instance.origin().y());
            statement.setDouble(8, instance.origin().z());
            statement.setFloat(9, instance.origin().yaw());
            statement.setFloat(10, instance.origin().pitch());
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
                while (rs.next()) {
                    results.add(TemporaryDimensionMapper.instance(rs));
                }
            }
        }
        return results;
    }

    public void insertParticipant(String instanceId, UUID playerUuid, NamedLocation returnLoc) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_dimension_participants
                     (instance_id, player_uuid, return_world, return_x, return_y, return_z, return_yaw, return_pitch)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, instanceId);
            statement.setString(2, playerUuid.toString());
            statement.setString(3, returnLoc.world());
            statement.setDouble(4, returnLoc.x());
            statement.setDouble(5, returnLoc.y());
            statement.setDouble(6, returnLoc.z());
            statement.setFloat(7, returnLoc.yaw());
            statement.setFloat(8, returnLoc.pitch());
            statement.executeUpdate();
        }
    }

    public void deleteParticipantsByInstance(String instanceId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_dimension_participants WHERE instance_id = ?")) {
            statement.setString(1, instanceId);
            statement.executeUpdate();
        }
    }

    public List<ParticipantRecord> listParticipants(String instanceId) throws Exception {
        List<ParticipantRecord> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT player_uuid, return_world, return_x, return_y, return_z, return_yaw, return_pitch
                     FROM temporary_dimension_participants WHERE instance_id = ?
                     """)) {
            statement.setString(1, instanceId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new ParticipantRecord(
                            UUID.fromString(rs.getString("player_uuid")),
                            TemporaryDimensionMapper.location(rs, "return_")));
                }
            }
        }
        return results;
    }

    public List<ParticipantReturn> findPendingReturns(UUID playerUuid) throws Exception {
        List<ParticipantReturn> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT p.return_world, p.return_x, p.return_y, p.return_z, p.return_yaw, p.return_pitch, p.instance_id
                     FROM temporary_dimension_participants p
                     JOIN temporary_dimension_instances i ON p.instance_id = i.instance_id
                     WHERE p.player_uuid = ? AND i.state = 'CLOSED'
                     """)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new ParticipantReturn(
                            rs.getString("instance_id"),
                            new NamedLocation("", rs.getString("return_world"),
                                    rs.getDouble("return_x"), rs.getDouble("return_y"),
                                    rs.getDouble("return_z"), rs.getFloat("return_yaw"),
                                    rs.getFloat("return_pitch"))));
                }
            }
        }
        return results;
    }

    public Optional<NamedLocation> findParticipantReturn(UUID playerUuid) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT p.return_world, p.return_x, p.return_y, p.return_z, p.return_yaw, p.return_pitch
                     FROM temporary_dimension_participants p
                     JOIN temporary_dimension_instances i ON p.instance_id = i.instance_id
                     WHERE p.player_uuid = ? AND i.state = 'ACTIVE'
                     LIMIT 1
                     """)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(TemporaryDimensionMapper.location(rs, "return_"));
                }
            }
        }
        return Optional.empty();
    }

    public void deleteParticipant(String instanceId, UUID playerUuid) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_dimension_participants WHERE instance_id = ? AND player_uuid = ?")) {
            statement.setString(1, instanceId);
            statement.setString(2, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    public void deleteClosedInstanceIfNoParticipants(String instanceId) throws Exception {
        try (var connection = database.open()) {
            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM temporary_dimension_participants WHERE instance_id = ?")) {
                count.setString(1, instanceId);
                try (ResultSet rs = count.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return;
                    }
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
    public record ParticipantRecord(UUID playerUuid, NamedLocation location) { }
}
