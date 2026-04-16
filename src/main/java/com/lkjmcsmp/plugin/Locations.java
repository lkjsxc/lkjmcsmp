package com.lkjmcsmp.plugin;

import com.lkjmcsmp.domain.model.NamedLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public final class Locations {
    private Locations() {
    }

    public static Optional<Location> toBukkit(NamedLocation source) {
        World world = Bukkit.getWorld(source.world());
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new Location(world, source.x(), source.y(), source.z(), source.yaw(), source.pitch()));
    }
}
