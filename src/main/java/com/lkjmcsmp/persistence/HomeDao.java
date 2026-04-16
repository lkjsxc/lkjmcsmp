package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.NamedLocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HomeDao {
    private final SqliteDatabase database;

    public HomeDao(SqliteDatabase database) {
        this.database = database;
    }

    public List<NamedLocation> list(UUID playerId) throws Exception {
        List<NamedLocation> homes = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT home_name, world, x, y, z, yaw, pitch
                     FROM homes WHERE player_uuid = ? ORDER BY home_name
                     """)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    homes.add(new NamedLocation(
                            rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5),
                            rs.getFloat(6), rs.getFloat(7)));
                }
            }
        }
        return homes;
    }

    public void upsert(UUID playerId, NamedLocation location) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(player_uuid, home_name) DO UPDATE SET
                      world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z, yaw=excluded.yaw, pitch=excluded.pitch
                    """)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, location.name().toLowerCase());
            statement.setString(3, location.world());
            statement.setDouble(4, location.x());
            statement.setDouble(5, location.y());
            statement.setDouble(6, location.z());
            statement.setFloat(7, location.yaw());
            statement.setFloat(8, location.pitch());
            statement.executeUpdate();
        }
    }

    public boolean delete(UUID playerId, String homeName) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM homes WHERE player_uuid = ? AND home_name = ?")) {
            statement.setString(1, playerId.toString());
            statement.setString(2, homeName.toLowerCase());
            return statement.executeUpdate() > 0;
        }
    }
}
