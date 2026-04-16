package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.persistence.WarpDao;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WarpService {
    private final WarpDao warpDao;

    public WarpService(WarpDao warpDao) {
        this.warpDao = warpDao;
    }

    public List<NamedLocation> list() throws Exception {
        return warpDao.list();
    }

    public Result setWarp(Player actor, String warpName) throws Exception {
        String normalized = normalize(warpName);
        if (normalized.isEmpty()) {
            return Result.fail("warp name is required");
        }
        Location loc = actor.getLocation();
        warpDao.upsert(actor.getUniqueId(), new NamedLocation(
                normalized,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()));
        return Result.ok("warp saved: " + normalized);
    }

    public Result deleteWarp(String warpName) throws Exception {
        return warpDao.delete(normalize(warpName))
                ? Result.ok("warp deleted")
                : Result.fail("warp not found");
    }

    public Optional<NamedLocation> findWarp(String warpName) throws Exception {
        String normalized = normalize(warpName);
        return warpDao.list().stream().filter(w -> w.name().equalsIgnoreCase(normalized)).findFirst();
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    public record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }
}
