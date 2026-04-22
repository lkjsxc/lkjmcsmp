# Temporary End Lifecycle

## States

1. `PENDING_CREATION` — purchase accepted, world creation queued.
2. `ACTIVE` — world exists, players may enter.
3. `EXPIRING` — duration elapsed; evacuation in progress.
4. `CLOSED` — world unloaded, records pending final cleanup.
5. `CLEANING_UP` — final deletion of world folder and DB records.

## Duration

1. Default duration: `3` hours.
2. Configurable under `temporary-end.duration-minutes`.
3. Expiry is checked every `5` minutes while instances are active.

## Expiry Sequence

1. Transition to `EXPIRING`.
2. Teleport all online participants in the world back to the recorded origin.
3. Unload the world with `save = false`.
4. Delete the world folder.
5. Transition to `CLOSED`, then `CLEANING_UP`.
6. Remove DB records.

## Offline and Join Handling

1. If a participant is offline during expiry, their return is deferred.
2. On next `PlayerJoinEvent`, if they have a participant record for a `CLOSED` instance, teleport them to the origin and delete the record.
3. This handles disconnect-during-expiry and server restart recovery.

## Startup Recovery

1. On startup, load all persisted `ACTIVE` instances.
2. If the world exists in `Bukkit.getWorlds()`, register it and schedule expiry checks.
3. If the world is missing, transition to `CLOSED` and clean up records.
4. Orphaned world folders without DB records are ignored.
