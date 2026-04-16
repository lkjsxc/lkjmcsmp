package com.lkjmcsmp.persistence;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.UUID;

public final class AuditDao {
    private final SqliteDatabase database;

    public AuditDao(SqliteDatabase database) {
        this.database = database;
    }

    public void log(UUID actor, UUID target, String eventKey, String beforeJson, String afterJson) throws Exception {
        try (var connection = database.open();
             PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO audit_log (id, actor_uuid, target_uuid, event_key, before_json, after_json, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, actor == null ? "SYSTEM" : actor.toString());
            statement.setString(3, target == null ? "NONE" : target.toString());
            statement.setString(4, eventKey);
            statement.setString(5, beforeJson);
            statement.setString(6, afterJson);
            statement.setString(7, Instant.now().toString());
            statement.executeUpdate();
        }
    }
}
