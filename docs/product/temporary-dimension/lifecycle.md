# Temporary Dimension Lifecycle

## Summary

Instances move through three states with deterministic expiry, evacuation, and cleanup.

## States

1. `ACTIVE` — world exists, players may enter.
2. `EXPIRING` — duration elapsed; evacuation in progress.
3. `CLOSED` — world unloaded and records removed.

## Participant States

1. `PENDING_TRANSFER` — return location is known; destination teleport has not succeeded.
2. `ACTIVE` — player reached the temporary dimension and must be returned.
3. `RETURN_PENDING` — instance is closed; return will retry on future join.

## Duration

1. Default duration: `3` hours.
2. Configurable under `temporary-dimension.duration-minutes`.
3. Expiry is checked every `5` minutes while instances are active.

## Expiry Sequence

1. Transition to `EXPIRING`.
2. Teleport all online `ACTIVE` participants in the world back to their recorded origin.
3. Teleport online untracked occupants to the main overworld spawn.
4. Unload the world with `save = false`.
5. Delete the world folder; if locked files prevent deletion, retry once after a short delay.
6. Transition to `CLOSED`.
7. Keep `CLOSED` instance and `RETURN_PENDING` participant rows while offline participants still have pending returns.
8. Remove the instance row only after all pending participant return rows are consumed.

## Offline and Join Handling

1. If an `ACTIVE` participant is offline during expiry, their state becomes `RETURN_PENDING`.
2. On next `PlayerJoinEvent`, if they have a `RETURN_PENDING` record for a `CLOSED` instance, teleport them to the origin and delete only that participant record after teleport success.
3. Failed join-time returns keep the row for a later retry.
4. Once the last participant record for a `CLOSED` instance is deleted, the instance row is removed.
5. This handles disconnect-during-expiry and server restart recovery.

## Death Handling

1. If a player dies inside a temporary dimension, their respawn location is overridden to their recorded origin.
2. If no origin record exists, the main overworld spawn is used.
3. This prevents respawning in a world that may be deleted on expiry.
4. This override takes precedence over normal respawn RTP.

## Portal Handling

1. `PlayerPortalEvent` inside any temporary dimension is cancelled.
2. End-portal teleport attempts inside temporary dimensions are also cancelled.
3. Portal attempts never remove participant return records.
4. This prevents escape to the main world network via Nether or End portals.

## Startup Recovery

1. On startup, load all persisted `ACTIVE` instances.
2. If the world exists in `Bukkit.getWorlds()`, register it and schedule expiry checks.
3. If the world is missing, transition to `CLOSED` and mark `ACTIVE` participant records as `RETURN_PENDING`.
4. Orphaned world folders without DB records are ignored.

## Multi-Instance Safety

1. Multiple provisional dimensions may exist simultaneously.
2. Each purchase (command, shop, or menu) creates an independent instance.
3. Instances do not interfere with each other; their world names are unique.
4. Expiry and evacuation are scoped per instance.

## Cross-References

- [access-rules.md](access-rules.md): gameplay inside active instances
- [purchase-and-creation.md](purchase-and-creation.md): how instances are created
