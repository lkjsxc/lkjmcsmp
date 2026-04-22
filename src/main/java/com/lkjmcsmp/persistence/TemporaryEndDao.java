package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryEndInstance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TemporaryEndDao {
    private final SqliteDatabase database;

    public TemporaryEndDao(SqliteDatabase database) {
        this.database = database;
    }

    public void insertInstance(TemporaryEndInstance instance) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_end_instances
                     (instance_id, world_name, creator_uuid, origin_world, origin_x, origin_y, origin_z, origin_yaw, origin_pitch, creation_time, expiration_time, state)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, instance.instanceId());
            statement.setString(2, instance.worldName());
            statement.setString(3, instance.creatorUuid().toString());
            statement.setString(4, instance.origin().world());
            statement.setDouble(5, instance.origin().x());
            statement.setDouble(6, instance.origin().y());
            statement.setDouble(7, instance.origin().z());
            statement.setFloat(8, instance.origin().yaw());
            statement.setFloat(9, instance.origin().pitch());
            statement.setString(10, instance.creationTime().toString());
            statement.setString(11, instance.expirationTime().toString());
            statement.setString(12, instance.state().name());
            statement.executeUpdate();
        }
    }

    public void updateState(String instanceId, InstanceLifecycle state) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE temporary_end_instances SET state = ? WHERE instance_id = ?")) {
            statement.setString(1, state.name());
            statement.setString(2, instanceId);
            statement.executeUpdate();
        }
    }

    public void deleteInstance(String instanceId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_end_instances WHERE instance_id = ?")) {
            statement.setString(1, instanceId);
            statement.executeUpdate();
        }
    }

    public List<TemporaryEndInstance> listByState(InstanceLifecycle state) throws Exception {
        List<TemporaryEndInstance> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM temporary_end_instances WHERE state = ?")) {
            statement.setString(1, state.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapInstance(rs));
                }
            }
        }
        return results;
    }

    public List<TemporaryEndInstance> listAll() throws Exception {
        List<TemporaryEndInstance> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM temporary_end_instances");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                results.add(mapInstance(rs));
            }
        }
        return results;
    }

    public void insertParticipant(String instanceId, UUID playerUuid, NamedLocation returnLoc) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_end_participants
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
                     "DELETE FROM temporary_end_participants WHERE instance_id = ?")) {
            statement.setString(1, instanceId);
            statement.executeUpdate();
        }
    }

    public List<ParticipantReturn> findPendingReturns(UUID playerUuid) throws Exception {
        List<ParticipantReturn> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT p.return_world, p.return_x, p.return_y, p.return_z, p.return_yaw, p.return_pitch, p.instance_id
                     FROM temporary_end_participants p
                     JOIN temporary_end_instances i ON p.instance_id = i.instance_id
                     WHERE p.player_uuid = ? AND i.state IN ('CLOSED','CLEANING_UP')
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

    public void deleteParticipant(String instanceId, UUID playerUuid) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_end_participants WHERE instance_id = ? AND player_uuid = ?")) {
            statement.setString(1, instanceId);
            statement.setString(2, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    private static TemporaryEndInstance mapInstance(ResultSet rs) throws Exception {
        return new TemporaryEndInstance(
                rs.getString("instance_id"),
                rs.getString("world_name"),
                UUID.fromString(rs.getString("creator_uuid")),
                new NamedLocation("", rs.getString("origin_world"),
                        rs.getDouble("origin_x"), rs.getDouble("origin_y"),
                        rs.getDouble("origin_z"), rs.getFloat("origin_yaw"),
                        rs.getFloat("origin_pitch")),
                Instant.parse(rs.getString("creation_time")),
                Instant.parse(rs.getString("expiration_time")),
                InstanceLifecycle.valueOf(rs.getString("state")));
    }

    public record ParticipantReturn(String instanceId, NamedLocation location) {
    }
}
