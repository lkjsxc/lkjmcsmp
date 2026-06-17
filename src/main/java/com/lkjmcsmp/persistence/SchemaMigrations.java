package com.lkjmcsmp.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class SchemaMigrations {
    private SchemaMigrations() {
    }

    static void ensurePlayerSettingsColumns(Connection connection) throws SQLException {
        addColumnIfMissing(
                connection,
                "player_settings",
                "action_bar_enabled",
                "INTEGER NOT NULL DEFAULT 1");
    }

    private static void addColumnIfMissing(
            Connection connection, String table, String column, String definition) throws SQLException {
        try (Statement statement = connection.createStatement();
             var rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
