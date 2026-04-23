# Temporary Dimension Lifecycle

## Summary

Instances move through three states with deterministic expiry, evacuation, and cleanup.

## States

1. `ACTIVE` — world exists, players may enter.
2. `EXPIRING` — duration elapsed; evacuation in progress.
3. `CLOSED` — world unloaded and records removed.

## Duration

1. Default duration: `3` hours.
2. Configurable under `temporary-dimension.duration-minutes`.
3. Expiry is checked every `5` minutes while instances are active.

## Expiry Sequence

1. Transition to `EXPIRING`.
2. Teleport all online participants in the world back to the recorded origin.
3. Unload the world with `save = false`.
4. Delete the world folder; if locked files prevent deletion, retry once after a short delay.
5. Transition to `CLOSED`.
6. Remove DB records.

## Offline and Join Handling

1. If a participant is offline during expiry, their return is deferred.
2. On next `PlayerJoinEvent`, if they have a participant record for a `CLOSED` instance, teleport them to the origin and delete the record.
3. The `CLOSED` state covers both record-pending and record-removed phases for simplicity.
4. This handles disconnect-during-expiry and server restart recovery.

## Startup Recovery

1. On startup, load all persisted `ACTIVE` instances.
2. If the world exists in `Bukkit.getWorlds()`, register it and schedule expiry checks.
3. If the world is missing, transition to `CLOSED` and clean up records.
4. Orphaned world folders without DB records are ignored.

## Multi-Instance Safety

1. Multiple provisional dimensions may exist simultaneously.
2. Each purchase (command, shop, or menu) creates an independent instance.
3. Instances do not interfere with each other; their world names are unique.
4. Expiry and evacuation are scoped per instance.

## Cross-References

- [access-rules.md](access-rules.md): gameplay inside active instances
- [purchase-and-creation.md](purchase-and-creation.md): how instances are created
