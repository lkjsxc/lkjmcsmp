package com.lkjmcsmp.plugin;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InitialSpawnRegistry {
    private final Map<String, Location> spawns = new ConcurrentHashMap<>();

    public InitialSpawnRegistry(Iterable<World> worlds) {
        for (World world : worlds) {
            spawns.put(world.getName(), world.getSpawnLocation().clone());
        }
    }

    public boolean isInitialSpawnBlock(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Location spawn = spawns.get(location.getWorld().getName());
        return spawn != null
                && location.getBlockX() == spawn.getBlockX()
                && location.getBlockY() == spawn.getBlockY()
                && location.getBlockZ() == spawn.getBlockZ();
    }
}
