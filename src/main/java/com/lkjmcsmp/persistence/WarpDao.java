package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.NamedLocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WarpDao {
    private final SqliteDatabase database;

    public WarpDao(SqliteDatabase database) {
        this.database = database;
    }

    public List<NamedLocation> list() throws Exception {
        List<NamedLocation> warps = new ArrayList<>();
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT warp_name, world, x, y, z, yaw, pitch
                     FROM warps ORDER BY warp_name
                     """);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                warps.add(new NamedLocation(
                        rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5),
                        rs.getFloat(6), rs.getFloat(7)));
            }
        }
        return warps;
    }

    public void upsert(UUID creator, NamedLocation location) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO warps (warp_name, world, x, y, z, yaw, pitch, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(warp_name) DO UPDATE SET
                      world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z, yaw=excluded.yaw, pitch=excluded.pitch
                    """)) {
            statement.setString(1, location.name().toLowerCase());
            statement.setString(2, location.world());
            statement.setDouble(3, location.x());
            statement.setDouble(4, location.y());
            statement.setDouble(5, location.z());
            statement.setFloat(6, location.yaw());
            statement.setFloat(7, location.pitch());
            statement.setString(8, creator.toString());
            statement.executeUpdate();
        }
    }

    public boolean delete(String warpName) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM warps WHERE warp_name = ?")) {
            statement.setString(1, warpName.toLowerCase());
            return statement.executeUpdate() > 0;
        }
    }
}
