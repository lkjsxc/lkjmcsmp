package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.NamedLocation;
import com.lkjmcsmp.persistence.HomeDao;
import com.lkjmcsmp.persistence.HomeSlotDao;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public final class HomeService {
    private final HomeDao homeDao;
    private final HomeSlotDao homeSlotDao;
    private final int baseMaxHomes;

    public HomeService(HomeDao homeDao, int maxHomes) {
        this(homeDao, null, maxHomes);
    }

    public HomeService(HomeDao homeDao, HomeSlotDao homeSlotDao, int maxHomes) {
        this.homeDao = homeDao;
        this.homeSlotDao = homeSlotDao;
        this.baseMaxHomes = maxHomes;
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
        int limit = maxHomes(player.getUniqueId());
        if (!alreadyExists && homes.size() >= limit) {
            return Result.fail("home limit reached (" + homes.size() + "/" + limit + ")");
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

    public Result setAutoHome(Player player) throws Exception {
        String nextName = nextHomeName(player.getUniqueId());
        return setHome(player, nextName);
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

    public int maxHomes(UUID playerId) throws Exception {
        return baseMaxHomes + purchasedHomeSlots(playerId);
    }

    public int purchasedHomeSlots(UUID playerId) throws Exception {
        return homeSlotDao == null
                ? 0
                : Math.min(homeSlotDao.getPurchasedSlots(playerId), HomeSlotCatalog.maxPurchasableSlots());
    }

    public OptionalInt purchaseAdditionalSlot(UUID playerId, int expectedPurchasedSlots) throws Exception {
        if (homeSlotDao == null || expectedPurchasedSlots >= HomeSlotCatalog.maxPurchasableSlots()) {
            return OptionalInt.empty();
        }
        OptionalInt purchasedSlots = homeSlotDao.purchaseNextSlot(playerId, expectedPurchasedSlots);
        return purchasedSlots.isPresent()
                ? OptionalInt.of(baseMaxHomes + purchasedSlots.getAsInt())
                : OptionalInt.empty();
    }

    private String nextHomeName(UUID playerId) throws Exception {
        List<NamedLocation> homes = homeDao.list(playerId);
        int index = 1;
        while (true) {
            String candidate = "home-" + index;
            boolean taken = homes.stream().anyMatch(home -> home.name().equalsIgnoreCase(candidate));
            if (!taken) {
                return candidate;
            }
            index++;
        }
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
