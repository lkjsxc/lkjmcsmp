package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.ParticipantLifecycle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class TemporaryDimensionParticipants {
    private final SqliteDatabase database;

    TemporaryDimensionParticipants(SqliteDatabase database) {
        this.database = database;
    }

    void insert(String instanceId, UUID playerUuid, ParticipantLifecycle state, NamedLocation returnLoc) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO temporary_dimension_participants
                     (instance_id, player_uuid, state, return_world, return_x, return_y, return_z,
                      return_yaw, return_pitch, created_at, updated_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            String now = Instant.now().toString();
            statement.setString(1, instanceId);
            statement.setString(2, playerUuid.toString());
            statement.setString(3, state.name());
            TemporaryDimensionMapper.bindLocation(statement, 4, returnLoc);
            statement.setString(10, now);
            statement.setString(11, now);
            statement.executeUpdate();
        }
    }

    void updateState(String instanceId, UUID playerUuid, ParticipantLifecycle state) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE temporary_dimension_participants
                     SET state = ?, updated_at = ?
                     WHERE instance_id = ? AND player_uuid = ?
                     """)) {
            statement.setString(1, state.name());
            statement.setString(2, Instant.now().toString());
            statement.setString(3, instanceId);
            statement.setString(4, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    void markActiveReturnPending(String instanceId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE temporary_dimension_participants
                     SET state = ?, updated_at = ?
                     WHERE instance_id = ? AND state = ?
                     """)) {
            statement.setString(1, ParticipantLifecycle.RETURN_PENDING.name());
            statement.setString(2, Instant.now().toString());
            statement.setString(3, instanceId);
            statement.setString(4, ParticipantLifecycle.ACTIVE.name());
            statement.executeUpdate();
        }
    }

    void deleteByInstance(String instanceId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_dimension_participants WHERE instance_id = ?")) {
            statement.setString(1, instanceId);
            statement.executeUpdate();
        }
    }

    List<TemporaryDimensionDao.ParticipantRecord> list(String instanceId, ParticipantLifecycle state) throws Exception {
        String filter = state == null ? "" : " AND state = ?";
        List<TemporaryDimensionDao.ParticipantRecord> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT player_uuid, state, return_world, return_x, return_y, return_z, return_yaw, return_pitch
                     FROM temporary_dimension_participants WHERE instance_id = ?""" + filter)) {
            statement.setString(1, instanceId);
            if (state != null) statement.setString(2, state.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) results.add(record(rs));
            }
        }
        return results;
    }

    List<TemporaryDimensionDao.ParticipantReturn> findPendingReturns(UUID playerUuid) throws Exception {
        List<TemporaryDimensionDao.ParticipantReturn> results = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT p.return_world, p.return_x, p.return_y, p.return_z, p.return_yaw, p.return_pitch, p.instance_id
                     FROM temporary_dimension_participants p
                     JOIN temporary_dimension_instances i ON p.instance_id = i.instance_id
                     WHERE p.player_uuid = ? AND p.state = ? AND i.state = 'CLOSED'
                     """)) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, ParticipantLifecycle.RETURN_PENDING.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new TemporaryDimensionDao.ParticipantReturn(
                            rs.getString("instance_id"), TemporaryDimensionMapper.location(rs, "return_")));
                }
            }
        }
        return results;
    }

    Optional<NamedLocation> findActiveReturn(UUID playerUuid) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT p.return_world, p.return_x, p.return_y, p.return_z, p.return_yaw, p.return_pitch
                     FROM temporary_dimension_participants p
                     JOIN temporary_dimension_instances i ON p.instance_id = i.instance_id
                     WHERE p.player_uuid = ? AND p.state = ? AND i.state = 'ACTIVE'
                     LIMIT 1
                     """)) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, ParticipantLifecycle.ACTIVE.name());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(TemporaryDimensionMapper.location(rs, "return_"));
            }
        }
        return Optional.empty();
    }

    void delete(String instanceId, UUID playerUuid) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM temporary_dimension_participants WHERE instance_id = ? AND player_uuid = ?")) {
            statement.setString(1, instanceId);
            statement.setString(2, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    Map<ParticipantLifecycle, Integer> countByState(String instanceId) throws Exception {
        Map<ParticipantLifecycle, Integer> counts = new EnumMap<>(ParticipantLifecycle.class);
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT state, COUNT(*) AS c FROM temporary_dimension_participants
                     WHERE instance_id = ? GROUP BY state
                     """)) {
            statement.setString(1, instanceId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) counts.put(ParticipantLifecycle.valueOf(rs.getString("state")), rs.getInt("c"));
            }
        }
        return counts;
    }

    private static TemporaryDimensionDao.ParticipantRecord record(ResultSet rs) throws Exception {
        return new TemporaryDimensionDao.ParticipantRecord(
                UUID.fromString(rs.getString("player_uuid")),
                ParticipantLifecycle.valueOf(rs.getString("state")),
                TemporaryDimensionMapper.location(rs, "return_"));
    }
}
