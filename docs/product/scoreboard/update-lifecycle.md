# Scoreboard Update Lifecycle

## Update Triggers

1. Player join: sidebar is attached immediately.
2. Plugin enable: sidebar state is attached/refreshed for already-online players.
3. Periodic refresh: online count and points are refreshed for all online players.
4. Player quit: player sidebar state is detached/reset.

## Scheduler Rules

1. Periodic refresh runs on a Folia-safe global scheduler path.
2. Player-specific scoreboard mutation runs in player-safe context.
3. Refresh failures are logged with player context.
4. Scoreboard mutations remain idempotent to avoid stale objective collisions.

## Performance Rules

1. Sidebar updates should avoid per-tick churn.
2. Refresh cadence must be coarse enough for stability (multi-second interval).
3. Points lookup failures use fallback value `0` instead of skipping render.
