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
    private final int rtpAttempts;
    private final List<String> worldWhitelist;
    private final RtpLocationSelector rtpSelector;
    private final TeleportExecutionService teleportExecution;
    private final TeleportHudSink teleportHudSink;
    private final PendingTeleportRequests pendingRequests = new PendingTeleportRequests();
    private final Map<UUID, Instant> rtpCooldownUntil = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public TeleportService(
            SchedulerBridge schedulerBridge,
            Duration requestTimeout,
            Duration rtpCooldown,
            Duration stabilityDelay,
            double stabilityRadiusBlocks,
            int rtpMinRadius,
            int rtpMaxRadius,
            int rtpAttempts,
            List<String> worldWhitelist,
            TeleportHudSink teleportHudSink) {
        this.schedulerBridge = schedulerBridge;
        this.requestTimeout = requestTimeout;
        this.rtpCooldown = rtpCooldown;
        this.rtpAttempts = rtpAttempts;
        this.worldWhitelist = worldWhitelist.stream().map(String::toLowerCase).toList();
        this.teleportHudSink = teleportHudSink == null ? TeleportHudSink.NO_OP : teleportHudSink;
        if (rtpMinRadius < 0 || rtpMaxRadius < rtpMinRadius) {
            throw new IllegalArgumentException("Invalid RTP radius range.");
        }
        if (rtpAttempts < 1) {
            throw new IllegalArgumentException("teleport.rtp-attempts must be >= 1");
        }
        if (stabilityDelay.isNegative()) {
            throw new IllegalArgumentException("teleport.stability-delay-seconds must be >= 0");
        }
        if (stabilityRadiusBlocks < 0) {
            throw new IllegalArgumentException("teleport.stability-radius-blocks must be >= 0");
        }
        this.rtpSelector = new RtpLocationSelector(rtpMinRadius, rtpMaxRadius, random);
        this.teleportExecution = new TeleportExecutionService(
                schedulerBridge,
                stabilityDelay,
                stabilityRadiusBlocks,
                this.teleportHudSink);
    }

    public long requestTimeoutSeconds() { return requestTimeout.getSeconds(); }
    public Result requestTeleport(Player from, Player to, boolean summonHere) {
        pendingRequests.put(new TpaRequest(from.getUniqueId(), to.getUniqueId(), summonHere, Instant.now().plus(requestTimeout)));
        return Result.ok("request sent");
    }
    public Result denyRequest(Player target) { return denyRequest(target, null); }
    public Result denyRequest(Player target, UUID requesterId) {
        return pendingRequests.remove(target.getUniqueId(), requesterId).isPresent()
                ? Result.ok("request denied")
                : Result.fail("no pending request");
    }
    public void acceptRequest(Player target, Consumer<Result> callback) { acceptRequest(target, null, callback); }
    public void acceptRequest(Player target, UUID requesterId, Consumer<Result> callback) {
        Optional<TpaRequest> removed = pendingRequests.remove(target.getUniqueId(), requesterId);
        if (removed.isEmpty()) {
            complete(target, callback, Result.fail("no pending request"));
            return;
        }
        TpaRequest request = removed.get();
        if (request.expiresAt().isBefore(Instant.now())) {
            complete(target, callback, Result.fail("request expired"));
            return;
        }
        Player from = Bukkit.getPlayer(request.from());
        if (from == null || !from.isOnline()) {
            complete(target, callback, Result.fail("requesting player is offline"));
            return;
        }
        Player actor = request.summonHere() ? target : from;
        Player source = request.summonHere() ? from : target;
        moveActorToSource(actor, source, target, "teleport request accepted", true, callback);
    }
    public void directTeleport(Player actor, Player target, Consumer<Result> callback) { moveActorToSource(actor, target, actor, "teleported to " + target.getName(), true, callback); }
    public void teleportToLocation(Player actor, Location destination, String successMessage, Consumer<Result> callback) { teleportToLocation(actor, destination, successMessage, true, callback); }
    public void teleportToLocation(Player actor, Location destination, String successMessage, boolean applyStabilityDelay,
                                   Consumer<Result> callback) {
        teleportExecution.teleport(actor, destination, successMessage, applyStabilityDelay, callback);
    }
    public void randomTeleport(Player player, String worldName, boolean bypassCooldown, Consumer<Result> callback) { randomTeleport(player, worldName, bypassCooldown, true, callback); }
    public void randomTeleport(Player player, String worldName, boolean bypassCooldown, boolean applyStabilityDelay,
                               Consumer<Result> callback) {
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
        runRandomTeleportAttempt(player, world, 1, applyStabilityDelay, callback);
    }
    public Optional<Location> selectRandomRespawnLocation(World world) {
        for (int attempt = 1; attempt <= rtpAttempts; attempt++) {
            Location probe = rtpSelector.createProbeLocation(world);
            Location destination = rtpSelector.resolveSafeDestination(world, probe.getBlockX(), probe.getBlockZ());
            if (destination != null) {
                return Optional.of(destination);
            }
        }
        return Optional.empty();
    }
    public List<TpaRequest> pendingFor(UUID targetId) { return pendingRequests.list(targetId); }
    public int pendingCount(UUID targetId) { return pendingFor(targetId).size(); }
    private void moveActorToSource(Player actor, Player source, Player callbackPlayer, String successMessage,
                                   boolean applyStabilityDelay, Consumer<Result> callback) {
        schedulerBridge.runPlayerTask(source, () -> {
            if (!source.isOnline()) {
                complete(callbackPlayer, callback, Result.fail("target offline"));
                return;
            }
            Location sourceLocation = source.getLocation().clone();
            teleportToLocation(
                    actor, sourceLocation, successMessage, applyStabilityDelay, result -> complete(callbackPlayer, callback, result));
        });
    }
    private World resolveWorld(String worldName) {
        String name = worldName == null || worldName.isBlank() ? "world" : worldName;
        if (!worldWhitelist.contains(name.toLowerCase())) return null;
        return Bukkit.getWorld(name);
    }
    private void runRandomTeleportAttempt(Player player, World world, int attempt, boolean applyStabilityDelay,
                                          Consumer<Result> callback) {
        if (attempt > rtpAttempts) {
            complete(player, callback, Result.fail("no safe random teleport location found"));
            return;
        }
        Location probe = rtpSelector.createProbeLocation(world);
        schedulerBridge.runRegionTask(probe, () -> {
            Location destination = rtpSelector.resolveSafeDestination(world, probe.getBlockX(), probe.getBlockZ());
            if (destination == null) {
                runRandomTeleportAttempt(player, world, attempt + 1, applyStabilityDelay, callback);
                return;
            }
            teleportToLocation(player, destination, "random teleport complete", applyStabilityDelay, result -> {
                if (!result.success()) {
                    callback.accept(result);
                    return;
                }
                rtpCooldownUntil.put(player.getUniqueId(), Instant.now().plus(rtpCooldown));
                callback.accept(result);
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
