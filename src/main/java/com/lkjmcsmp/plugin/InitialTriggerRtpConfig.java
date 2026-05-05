package com.lkjmcsmp.plugin;

import org.bukkit.configuration.ConfigurationSection;

record InitialTriggerRtpConfig(
        String triggerWorld,
        String targetWorld,
        double centerX,
        double centerZ,
        double radius,
        int countdownSeconds,
        boolean cancelOnExit) {
    static InitialTriggerRtpConfig from(ConfigurationSection section) {
        return new InitialTriggerRtpConfig(
                section.getString("trigger-world", "world"),
                section.getString("target-world", "world"),
                section.getDouble("center-x", 0.0D),
                section.getDouble("center-z", 0.0D),
                section.getDouble("trigger-radius-blocks", 200.0D),
                section.getInt("countdown-seconds", 5),
                section.getBoolean("cancel-on-exit", true));
    }
}
