package com.lkjmcsmp.persistence;

import com.lkjmcsmp.domain.model.InstanceLifecycle;
import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.domain.model.TemporaryDimensionInstance;
import org.bukkit.World;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.UUID;

final class TemporaryDimensionMapper {
    private TemporaryDimensionMapper() {
    }

    static NamedLocation location(ResultSet rs, String prefix) throws Exception {
        return new NamedLocation("", rs.getString(prefix + "world"),
                rs.getDouble(prefix + "x"), rs.getDouble(prefix + "y"), rs.getDouble(prefix + "z"),
                rs.getFloat(prefix + "yaw"), rs.getFloat(prefix + "pitch"));
    }

    static void bindLocation(java.sql.PreparedStatement statement, int start, NamedLocation loc) throws Exception {
        statement.setString(start, loc.world());
        statement.setDouble(start + 1, loc.x());
        statement.setDouble(start + 2, loc.y());
        statement.setDouble(start + 3, loc.z());
        statement.setFloat(start + 4, loc.yaw());
        statement.setFloat(start + 5, loc.pitch());
    }

    static TemporaryDimensionInstance instance(ResultSet rs) throws Exception {
        return new TemporaryDimensionInstance(
                rs.getString("instance_id"),
                rs.getString("world_name"),
                UUID.fromString(rs.getString("creator_uuid")),
                environment(rs.getString("environment")),
                location(rs, "origin_"),
                Instant.parse(rs.getString("creation_time")),
                Instant.parse(rs.getString("expiration_time")),
                InstanceLifecycle.valueOf(rs.getString("state")));
    }

    private static World.Environment environment(String value) {
        try {
            return World.Environment.valueOf(value);
        } catch (Exception e) {
            return World.Environment.THE_END;
        }
    }
}
