package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.persistence.HomeDao;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class HomeService {
    private final HomeDao homeDao;
    private final int maxHomes;

    public HomeService(HomeDao homeDao, int maxHomes) {
        this.homeDao = homeDao;
        this.maxHomes = maxHomes;
    }

    public List<NamedLocation> list(UUID playerId) throws Exception {
        return homeDao.list(playerId);
    }

    public Result setHome(Player player, String homeName) throws Exception {
        String normalized = normalize(homeName);
        if (normalized.isEmpty()) {
            return Result.fail("home name is required");
        }
        List<NamedLocation> homes = homeDao.list(player.getUniqueId());
        boolean alreadyExists = homes.stream().anyMatch(h -> h.name().equalsIgnoreCase(normalized));
        if (!alreadyExists && homes.size() >= maxHomes) {
            return Result.fail("home limit reached");
        }
        Location loc = player.getLocation();
        homeDao.upsert(player.getUniqueId(), new NamedLocation(
                normalized,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()));
        return Result.ok("home saved: " + normalized);
    }

    public Result deleteHome(UUID playerId, String homeName) throws Exception {
        if (homeDao.delete(playerId, normalize(homeName))) {
            return Result.ok("home deleted");
        }
        return Result.fail("home not found");
    }

    public Optional<NamedLocation> findHome(UUID playerId, String homeName) throws Exception {
        return homeDao.list(playerId).stream()
                .filter(home -> home.name().equalsIgnoreCase(normalize(homeName)))
                .findFirst();
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
