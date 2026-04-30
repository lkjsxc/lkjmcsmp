package com.lkjmcsmp.plugin.temporarydimension;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;

public final class TemporaryDimensionWorldFactory {
    private final Logger logger;

    public TemporaryDimensionWorldFactory(Logger logger) {
        this.logger = logger;
    }

    public World createWorld(String worldName, World.Environment environment) {
        World world = create(worldName, environment);
        if (world == null && environment != World.Environment.NORMAL) {
            logger.warning("Falling back to NORMAL temporary dimension world for " + worldName);
            world = create(worldName, World.Environment.NORMAL);
        }
        if (world != null) {
            world.setAutoSave(false);
            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, true);
        }
        return world;
    }

    private World create(String worldName, World.Environment environment) {
        try {
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            return creator.createWorld();
        } catch (RuntimeException ex) {
            logger.warning("Temporary dimension world create failed for " + environment + ": " + ex.getMessage());
            return null;
        }
    }

    public Location resolveSpawnLocation(World world) {
        return switch (world.getEnvironment()) {
            case THE_END -> new Location(world, 100.5, 49, 0.5);
            default -> {
                int x = 0;
                int z = 0;
                int y = world.getHighestBlockYAt(x, z);
                yield new Location(world, x + 0.5, Math.max(y, 70), z + 0.5);
            }
        };
    }

    public boolean unloadAndDelete(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
        Path worldFolder = Bukkit.getWorldContainer().toPath().resolve(worldName);
        if (Files.exists(worldFolder)) {
            try {
                deleteRecursive(worldFolder);
                return true;
            } catch (IOException e) {
                logger.warning("Failed to delete world folder " + worldName + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var entries = Files.list(path)) {
                entries.sorted(Comparator.reverseOrder())
                        .forEach(child -> {
                            try {
                                deleteRecursive(child);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        Files.delete(path);
    }
}
