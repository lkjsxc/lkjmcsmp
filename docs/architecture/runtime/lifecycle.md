# Lifecycle Contract

## Enable Sequence

1. Load config defaults and validate required keys.
2. Initialize SQLite connection and schema migrator.
3. Construct domain services and repository adapters.
4. Register commands.
5. Register GUI listeners (inventory + hotbar menu entrypoint).
6. Register teleport override and first-join listeners.
7. Construct `SmpScoreboardService` with scheduler bridge + points dependencies.
8. Register scoreboard join/quit lifecycle listeners.
9. Run startup scoreboard reconcile for already-online players.
10. Start periodic scoreboard reconcile scheduler.
11. Emit startup summary log including scoreboard scheduler state.

## Disable Sequence

1. Detach scoreboard state for online players and clear retry queues.
2. Flush pending audit buffer.
3. Close persistence resources.
4. Cancel scheduled tasks owned by plugin.
5. Emit shutdown summary log.

## Runtime Threading Rules

1. Scoreboard mutations execute only in player-safe scheduler context.
2. Startup/periodic reconcile orchestration executes in global scheduler context.
3. Data lookups execute off gameplay mutation path and feed immutable snapshots back to player tasks.

## Failure Rules

1. Schema init failure aborts plugin enable.
2. Missing mandatory command registration aborts plugin enable.
3. Scoreboard listener/scheduler startup failure aborts plugin enable.
4. Partial initialization must be rolled back before returning failure.
5. Runtime scoreboard render failures must trigger deterministic retry/rebuild with structured logging.
