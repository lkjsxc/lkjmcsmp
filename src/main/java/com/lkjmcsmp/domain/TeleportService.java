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
import java.util.function.Consumer;

public final class TeleportService {
    private final SchedulerBridge schedulerBridge;
    private final Duration requestTimeout;
    private final Duration rtpCooldown;
    private final int rtpMinRadius;
    private final int rtpMaxRadius;
    private final int rtpAttempts;
    private final List<String> worldWhitelist;
    private final RtpLocationSelector rtpSelector;
    private final Map<UUID, TpaRequest> pendingByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> rtpCooldownUntil = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public TeleportService(
            SchedulerBridge schedulerBridge,
            Duration requestTimeout,
            Duration rtpCooldown,
            int rtpMinRadius,
            int rtpMaxRadius,
            int rtpAttempts,
            List<String> worldWhitelist) {
        this.schedulerBridge = schedulerBridge;
        this.requestTimeout = requestTimeout;
        this.rtpCooldown = rtpCooldown;
        this.rtpMinRadius = rtpMinRadius;
        this.rtpMaxRadius = rtpMaxRadius;
        this.rtpAttempts = rtpAttempts;
        this.worldWhitelist = worldWhitelist.stream().map(String::toLowerCase).toList();
        if (rtpMinRadius < 0 || rtpMaxRadius < rtpMinRadius) {
            throw new IllegalArgumentException("Invalid RTP radius range.");
        }
        if (rtpAttempts < 1) {
            throw new IllegalArgumentException("teleport.rtp-attempts must be >= 1");
        }
        this.rtpSelector = new RtpLocationSelector(rtpMinRadius, rtpMaxRadius, random);
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

    public void acceptRequest(Player target, Consumer<Result> callback) {
        TpaRequest request = pendingByTarget.remove(target.getUniqueId());
        if (request == null) {
            complete(target, callback, Result.fail("no pending request"));
            return;
        }
        if (request.expiresAt().isBefore(Instant.now())) {
            complete(target, callback, Result.fail("request expired"));
            return;
        }
        Player from = Bukkit.getPlayer(request.from());
        if (from == null || !from.isOnline()) {
            complete(target, callback, Result.fail("requesting player is offline"));
            return;
        }
        Player actor = request.summonHere() ? from : target;
        Player source = request.summonHere() ? target : from;
        moveActorToSource(actor, source, target, "teleport request accepted", callback);
    }

    public void directTeleport(Player actor, Player target, Consumer<Result> callback) {
        moveActorToSource(actor, target, actor, "teleported to " + target.getName(), callback);
    }

    public void randomTeleport(Player player, String worldName, boolean bypassCooldown, Consumer<Result> callback) {
        if (!bypassCooldown) {
            Instant until = rtpCooldownUntil.getOrDefault(player.getUniqueId(), Instant.EPOCH);
            if (until.isAfter(Instant.now())) {
                long seconds = Duration.between(Instant.now(), until).toSeconds();
                complete(player, callback, Result.fail("rtp cooldown active: " + seconds + "s"));
                return;
            }
        }
        World world = resolveWorld(worldName);
        if (world == null) {
            complete(player, callback, Result.fail("rtp world is not allowed"));
            return;
        }
        runRandomTeleportAttempt(player, world, 1, callback);
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

    private void moveActorToSource(
            Player actor,
            Player source,
            Player callbackPlayer,
            String successMessage,
            Consumer<Result> callback) {
        schedulerBridge.runPlayerTask(source, () -> {
            if (!source.isOnline()) {
                complete(callbackPlayer, callback, Result.fail("target offline"));
                return;
            }
            Location sourceLocation = source.getLocation().clone();
            schedulerBridge.runPlayerTask(actor, () -> {
                if (!actor.isOnline()) {
                    complete(callbackPlayer, callback, Result.fail("actor went offline"));
                    return;
                }
                boolean moved = actor.teleport(sourceLocation);
                complete(callbackPlayer, callback, moved ? Result.ok(successMessage) : Result.fail("teleport failed"));
            });
        });
    }

    private void runRandomTeleportAttempt(Player player, World world, int attempt, Consumer<Result> callback) {
        if (attempt > rtpAttempts) {
            complete(player, callback, Result.fail("no safe random teleport location found"));
            return;
        }
        Location probe = rtpSelector.createProbeLocation(world);
        schedulerBridge.runRegionTask(probe, () -> {
            Location destination = rtpSelector.resolveSafeDestination(world, probe.getBlockX(), probe.getBlockZ());
            if (destination == null) {
                runRandomTeleportAttempt(player, world, attempt + 1, callback);
                return;
            }
            schedulerBridge.runPlayerTask(player, () -> {
                if (!player.isOnline()) {
                    complete(player, callback, Result.fail("player went offline"));
                    return;
                }
                boolean moved = player.teleport(destination);
                if (!moved) {
                    complete(player, callback, Result.fail("random teleport failed"));
                    return;
                }
                rtpCooldownUntil.put(player.getUniqueId(), Instant.now().plus(rtpCooldown));
                complete(player, callback, Result.ok("random teleport complete"));
            });
        });
    }

    private void complete(Player player, Consumer<Result> callback, Result result) {
        schedulerBridge.runPlayerTask(player, () -> callback.accept(result));
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
