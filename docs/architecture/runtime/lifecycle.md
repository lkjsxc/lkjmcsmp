# Lifecycle Contract

## Enable Sequence

1. Load config defaults and validate required keys.
2. Initialize SQLite connection and schema migrator.
3. Construct domain services and repository adapters.
4. Register commands.
5. Register GUI listeners (inventory + hotbar menu entrypoint).
6. Register teleport override and first-join listeners.
7. Construct `ActionBarHudService` with scheduler bridge + points dependencies.
8. Register HUD listeners for join/quit and combat triggers.
9. Prime idle HUD state for already-online players.
10. Emit startup summary log including HUD state handlers.

## Disable Sequence

1. Clear transient HUD overlays for online players.
2. Flush pending audit buffer.
3. Close persistence resources.
4. Cancel scheduled tasks owned by plugin.
5. Emit shutdown summary log.

## Runtime Threading Rules

1. Action-bar mutations execute only in player-safe scheduler context.
2. Overlay expiry scheduling is player-scoped via delayed player scheduling.
3. Data lookups execute off gameplay mutation path and feed immutable snapshots back to player tasks.

## Failure Rules

1. Schema init failure aborts plugin enable.
2. Missing mandatory command registration aborts plugin enable.
3. HUD listener/scheduler startup failure aborts plugin enable.
4. Partial initialization must be rolled back before returning failure.
5. Runtime HUD update failures must surface deterministic fallback to lower-priority state with structured logging.
