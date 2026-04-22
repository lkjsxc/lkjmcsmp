package com.lkjmcsmp.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TemporaryEndInstance(
        String instanceId,
        String worldName,
        UUID creatorUuid,
        NamedLocation origin,
        Instant creationTime,
        Instant expirationTime,
        InstanceLifecycle state) {
}
