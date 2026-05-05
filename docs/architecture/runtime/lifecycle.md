# Lifecycle Contract

## Enable Sequence

1. Load config defaults and validate required keys.
2. Initialize SQLite connection and schema migrator.
3. Construct domain services and repository adapters.
4. Bootstrap `TemporaryDimensionManager` and run startup recovery for active instances.
5. Register commands.
6. Register GUI listeners (inventory + hotbar menu entrypoint).
7. Register teleport override and repeatable initial trigger RTP listeners.
8. Construct `ActionBarRouter` with scheduler bridge + points dependencies.
9. Register HUD listeners for join/quit and combat triggers.
10. Prime idle HUD state for already-online players and start per-player periodic tasks.
11. Emit startup summary log including HUD state handlers and temporary dimension recovery count.

## Disable Sequence

1. Stop `ActionBarRouter` and cancel all per-player periodic tasks.
2. Close the SQLite database connection.
3. Emit shutdown summary log.

## Runtime Threading Rules

1. Action-bar mutations execute only in player-safe scheduler context.
2. HUD re-evaluation uses per-player periodic tasks; no global tick orchestration.
3. Data lookups execute off gameplay mutation path and feed immutable snapshots back to player tasks.

## Failure Rules

1. Schema init failure aborts plugin enable.
2. Missing mandatory command registration aborts plugin enable.
3. HUD listener/scheduler startup failure aborts plugin enable.
4. Partial initialization must be rolled back before returning failure.
5. Runtime HUD update failures must surface deterministic fallback to lower-priority state with structured logging.
