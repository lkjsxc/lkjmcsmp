package com.lkjmcsmp.plugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InitialTriggerRtpConfigTest {
    @Test
    void readsConfiguredTriggerZone() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("trigger-world", "spawn");
        yaml.set("target-world", "world");
        yaml.set("center-x", 10);
        yaml.set("center-z", -20);
        yaml.set("trigger-radius-blocks", 150);
        yaml.set("countdown-seconds", 7);
        yaml.set("cancel-on-exit", false);
        yaml.set("once-per-player", false);

        InitialTriggerRtpConfig config = InitialTriggerRtpConfig.from(yaml);

        assertEquals("spawn", config.triggerWorld());
        assertEquals("world", config.targetWorld());
        assertEquals(10, config.centerX());
        assertEquals(-20, config.centerZ());
        assertEquals(150, config.radius());
        assertEquals(7, config.countdownSeconds());
        assertFalse(config.cancelOnExit());
        assertFalse(config.oncePerPlayer());
    }
}
