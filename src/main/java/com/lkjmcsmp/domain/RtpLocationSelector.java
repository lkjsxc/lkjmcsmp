package com.lkjmcsmp.domain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

final class RtpLocationSelector {
    private final int minRadius;
    private final int maxRadius;
    private final Random random;

    RtpLocationSelector(int minRadius, int maxRadius, Random random) {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.random = random;
    }

    Location createProbeLocation(World world) {
        Location center = world.getSpawnLocation();
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        double angle = random.nextDouble() * (Math.PI * 2.0);
        int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
        int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);
        return new Location(world, x, center.getY(), z);
    }

    Location resolveSafeDestination(World world, int x, int z) {
        int baseY = world.getHighestBlockYAt(x, z);
        int feetY = baseY + 1;
        Material support = world.getBlockAt(x, baseY, z).getType();
        Material feet = world.getBlockAt(x, feetY, z).getType();
        Material head = world.getBlockAt(x, feetY + 1, z).getType();
        if (feet.isSolid() || head.isSolid() || isUnsafeSupport(support)) {
            return null;
        }
        return new Location(world, x + 0.5D, feetY, z + 0.5D);
    }

    private boolean isUnsafeSupport(Material support) {
        return support == Material.LAVA
                || support == Material.WATER
                || support == Material.MAGMA_BLOCK
                || support == Material.CAMPFIRE
                || support == Material.SOUL_CAMPFIRE
                || support == Material.CACTUS
                || support == Material.SWEET_BERRY_BUSH
                || support == Material.FIRE;
    }
}
