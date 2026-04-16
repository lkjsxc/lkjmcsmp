package com.lkjmcsmp.domain;

import com.lkjmcsmp.domain.model.TpaRequest;
import com.lkjmcsmp.plugin.SchedulerBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportService {
    private final SchedulerBridge schedulerBridge;
    private final Duration requestTimeout;
    private final Duration rtpCooldown;
    private final int rtpRadius;
    private final List<String> worldWhitelist;
    private final Map<UUID, TpaRequest> pendingByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> rtpCooldownUntil = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public TeleportService(
            SchedulerBridge schedulerBridge,
            Duration requestTimeout,
            Duration rtpCooldown,
            int rtpRadius,
            List<String> worldWhitelist) {
        this.schedulerBridge = schedulerBridge;
        this.requestTimeout = requestTimeout;
        this.rtpCooldown = rtpCooldown;
        this.rtpRadius = rtpRadius;
        this.worldWhitelist = worldWhitelist.stream().map(String::toLowerCase).toList();
    }

    public Result requestTeleport(Player from, Player to, boolean summonHere) {
        pendingByTarget.put(to.getUniqueId(),
                new TpaRequest(from.getUniqueId(), to.getUniqueId(), summonHere, Instant.now().plus(requestTimeout)));
        return Result.ok("request sent");
    }

    public Result denyRequest(Player target) {
        TpaRequest removed = pendingByTarget.remove(target.getUniqueId());
        return removed == null ? Result.fail("no pending request") : Result.ok("request denied");
    }

    public Result acceptRequest(Player target) {
        TpaRequest request = pendingByTarget.remove(target.getUniqueId());
        if (request == null) {
            return Result.fail("no pending request");
        }
        if (request.expiresAt().isBefore(Instant.now())) {
            return Result.fail("request expired");
        }
        Player from = Bukkit.getPlayer(request.from());
        if (from == null || !from.isOnline()) {
            return Result.fail("requesting player is offline");
        }
        if (request.summonHere()) {
            schedulerBridge.runPlayerTask(from, () -> from.teleport(target.getLocation()));
        } else {
            schedulerBridge.runPlayerTask(target, () -> target.teleport(from.getLocation()));
        }
        return Result.ok("teleport request accepted");
    }

    public Result directTeleport(Player actor, Player target) {
        schedulerBridge.runPlayerTask(actor, () -> actor.teleport(target.getLocation()));
        return Result.ok("teleported to " + target.getName());
    }

    public Result randomTeleport(Player player, String worldName, boolean bypassCooldown) {
        if (!bypassCooldown) {
            Instant until = rtpCooldownUntil.getOrDefault(player.getUniqueId(), Instant.EPOCH);
            if (until.isAfter(Instant.now())) {
                long seconds = Duration.between(Instant.now(), until).toSeconds();
                return Result.fail("rtp cooldown active: " + seconds + "s");
            }
        }
        World world = resolveWorld(worldName);
        if (world == null) {
            return Result.fail("rtp world is not allowed");
        }
        int x = random.nextInt(rtpRadius * 2 + 1) - rtpRadius;
        int z = random.nextInt(rtpRadius * 2 + 1) - rtpRadius;
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location target = new Location(world, x + 0.5, y, z + 0.5);
        schedulerBridge.runPlayerTask(player, () -> player.teleport(target));
        rtpCooldownUntil.put(player.getUniqueId(), Instant.now().plus(rtpCooldown));
        return Result.ok("random teleport complete");
    }

    private World resolveWorld(String worldName) {
        String name = worldName == null || worldName.isBlank() ? "world" : worldName;
        if (!worldWhitelist.contains(name.toLowerCase())) {
            return null;
        }
        return Bukkit.getWorld(name);
    }

    public Optional<TpaRequest> pendingFor(UUID targetId) {
        return Optional.ofNullable(pendingByTarget.get(targetId));
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
