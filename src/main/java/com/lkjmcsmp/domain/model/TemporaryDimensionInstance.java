package com.lkjmcsmp.domain.model;

import org.bukkit.World;

import java.time.Instant;
import java.util.UUID;

public record TemporaryDimensionInstance(
        String instanceId,
        String worldName,
        UUID creatorUuid,
        World.Environment environment,
        NamedLocation origin,
        Instant creationTime,
        Instant expirationTime,
        InstanceLifecycle state) {
}
