package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.NamedLocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PartyDao {
    private final SqliteDatabase database;

    public PartyDao(SqliteDatabase database) {
        this.database = database;
    }

    public Optional<String> findPartyId(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT party_id FROM party_members WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }

    public Optional<String> findRole(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT role FROM party_members WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }

    public void createParty(String partyId, UUID leader) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO party_members (party_id, player_uuid, role) VALUES (?, ?, ?)
                    """)) {
            statement.setString(1, partyId);
            statement.setString(2, leader.toString());
            statement.setString(3, "leader");
            statement.executeUpdate();
        }
    }

    public void upsertInvite(String partyId, UUID target, UUID invitedBy, Instant expiresAt) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO party_invites (party_id, target_uuid, invited_by, expires_at)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(target_uuid) DO UPDATE SET
                      party_id=excluded.party_id, invited_by=excluded.invited_by, expires_at=excluded.expires_at
                    """)) {
            statement.setString(1, partyId);
            statement.setString(2, target.toString());
            statement.setString(3, invitedBy.toString());
            statement.setString(4, expiresAt.toString());
            statement.executeUpdate();
        }
    }

    public Optional<InviteRecord> getInvite(UUID target) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT party_id, invited_by, expires_at FROM party_invites WHERE target_uuid = ?
                     """)) {
            statement.setString(1, target.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new InviteRecord(
                        rs.getString(1), UUID.fromString(rs.getString(2)), Instant.parse(rs.getString(3))));
            }
        }
    }

    public void deleteInvite(UUID target) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM party_invites WHERE target_uuid=?")) {
            statement.setString(1, target.toString());
            statement.executeUpdate();
        }
    }

    public void joinParty(String partyId, UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO party_members (party_id, player_uuid, role) VALUES (?, ?, ?)
                    ON CONFLICT(player_uuid) DO UPDATE SET party_id = excluded.party_id, role = excluded.role
                    """)) {
            statement.setString(1, partyId);
            statement.setString(2, playerId.toString());
            statement.setString(3, "member");
            statement.executeUpdate();
        }
    }

    public List<UUID> listMembers(String partyId) throws Exception {
        List<UUID> members = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT player_uuid FROM party_members WHERE party_id = ?")) {
            statement.setString(1, partyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    members.add(UUID.fromString(rs.getString(1)));
                }
            }
        }
        return members;
    }

    public void removeMember(UUID playerId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM party_members WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        }
    }

    public void disband(String partyId) throws Exception {
        try (var connection = database.open();
             PreparedStatement members = connection.prepareStatement("DELETE FROM party_members WHERE party_id = ?");
             PreparedStatement invites = connection.prepareStatement("DELETE FROM party_invites WHERE party_id = ?");
             PreparedStatement home = connection.prepareStatement("DELETE FROM party_homes WHERE party_id = ?")) {
            members.setString(1, partyId);
            members.executeUpdate();
            invites.setString(1, partyId);
            invites.executeUpdate();
            home.setString(1, partyId);
            home.executeUpdate();
        }
    }

    public void upsertHome(String partyId, NamedLocation location) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO party_homes (party_id, world, x, y, z, yaw, pitch)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(party_id) DO UPDATE SET
                      world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z, yaw=excluded.yaw, pitch=excluded.pitch
                    """)) {
            statement.setString(1, partyId);
            statement.setString(2, location.world());
            statement.setDouble(3, location.x());
            statement.setDouble(4, location.y());
            statement.setDouble(5, location.z());
            statement.setFloat(6, location.yaw());
            statement.setFloat(7, location.pitch());
            statement.executeUpdate();
        }
    }

    public Optional<NamedLocation> getHome(String partyId) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    SELECT world, x, y, z, yaw, pitch FROM party_homes WHERE party_id = ?
                    """)) {
            statement.setString(1, partyId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new NamedLocation(
                        "party", rs.getString(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getFloat(5), rs.getFloat(6)));
            }
        }
    }

    public record InviteRecord(String partyId, UUID invitedBy, Instant expiresAt) {
    }
}
