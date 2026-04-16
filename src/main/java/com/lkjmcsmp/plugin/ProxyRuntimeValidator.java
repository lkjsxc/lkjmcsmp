package com.lkjmcsmp.plugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public final class ProxyRuntimeValidator {
    private ProxyRuntimeValidator() {
    }

    public static void validate(JavaPlugin plugin) {
        Logger logger = plugin.getLogger();
        File serverRoot = plugin.getDataFolder().getParentFile().getParentFile();
        File paperGlobal = new File(serverRoot, "config/paper-global.yml");
        File spigot = new File(serverRoot, "spigot.yml");
        File serverProperties = new File(serverRoot, "server.properties");

        boolean velocityEnabled = readYamlBoolean(paperGlobal, "proxies.velocity.enabled", false);
        String velocitySecret = readYamlString(paperGlobal, "proxies.velocity.secret", "");
        boolean spigotBungee = readYamlBoolean(spigot, "settings.bungeecord", false);
        boolean preventProxyConnections = readServerPropertiesBoolean(
                logger, serverProperties, "prevent-proxy-connections", false);

        if (velocityEnabled) {
            logger.info("Proxy validation: Velocity mode enabled (config/paper-global.yml:proxies.velocity.enabled).");
            if (velocitySecret.isBlank()) {
                logger.severe("Proxy validation: velocity secret is blank (config/paper-global.yml:proxies.velocity.secret).");
            }
            if (preventProxyConnections) {
                logger.warning("Proxy validation: prevent-proxy-connections=true may block proxy clients (server.properties).");
            }
        }
        if (velocityEnabled && spigotBungee) {
            logger.warning("Proxy validation: both Velocity and bungeecord modes appear enabled (paper-global + spigot.yml).");
        }
        if (!velocityEnabled && !spigotBungee) {
            logger.info("Proxy validation: running without proxy forwarding mode.");
        }
    }

    private static boolean readYamlBoolean(File file, String path, boolean fallback) {
        if (!file.exists()) {
            return fallback;
        }
        return YamlConfiguration.loadConfiguration(file).getBoolean(path, fallback);
    }

    private static String readYamlString(File file, String path, String fallback) {
        if (!file.exists()) {
            return fallback;
        }
        return YamlConfiguration.loadConfiguration(file).getString(path, fallback);
    }

    private static boolean readServerPropertiesBoolean(Logger logger, File file, String key, boolean fallback) {
        if (!file.exists()) {
            return fallback;
        }
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(fallback)));
        } catch (Exception ex) {
            logger.warning("Proxy validation: failed to read " + file.getName() + " key " + key + ": " + ex.getMessage());
            return fallback;
        }
    }
}
