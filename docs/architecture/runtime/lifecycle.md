# Lifecycle Contract

## Enable Sequence

1. Load config defaults and validate required keys.
2. Initialize SQLite connection and schema migrator.
3. Construct domain services and repository adapters.
4. Register commands.
5. Register GUI listeners (inventory + hotbar menu entrypoint).
6. Register teleport override and first-join listeners.
7. Start scoreboard refresh scheduler and register scoreboard lifecycle listener.
8. Emit startup summary log.

## Disable Sequence

1. Flush pending audit buffer.
2. Close persistence resources.
3. Cancel scheduled tasks owned by plugin.
4. Emit shutdown summary log.

## Failure Rules

1. Schema init failure aborts plugin enable.
2. Missing mandatory command registration aborts plugin enable.
3. Partial initialization must be rolled back before returning failure.
