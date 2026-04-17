# Scoreboard Update Lifecycle

## Update Triggers

1. Player join: enqueue immediate player-scoped render.
2. Plugin enable: register already-online players and start per-player reconcile loops.
3. Periodic reconcile: each tracked player runs reconcile on fixed `5s` cadence through player-scoped delayed scheduling.
4. Targeted refresh: optional data-change refreshes must use the same render pipeline as join/reconcile.
5. Player quit: detach/reset scoreboard state, stop player reconcile loop, and drop tracked online state.

## Folia Context Rules

1. Reconcile orchestration is player-scoped; global tick orchestration is not assumed.
2. All Bukkit scoreboard mutations run on player-safe scheduler context for the target player.
3. Persistence/data reads run off player thread and return immutable render snapshots.
4. Player-scoped tasks must re-check player online state before mutating scoreboard.
5. No cross-player Bukkit entity reads are allowed inside player mutation tasks.
6. Online-count data is sourced from tracked join/quit state shared as immutable snapshot values.

## Deterministic Render Pipeline

1. Build snapshot: `onlineCount`, `points`, and fallback defaults (`points=0`) when lookup fails.
2. Ensure board primitives exist using fixed IDs from layout contract.
3. Apply title + required lines in canonical order only.
4. Set `DisplaySlot.SIDEBAR` on managed objective every render (always reclaim ownership).
5. Verify required objective + lines exist; treat any mismatch as a render failure.

## Recovery Strategy

1. On mutation failure, log `WARN` with `trigger`, `playerUuid`, `attempt`, and failure summary.
2. Retry with deterministic backoff per trigger: `20`, `100`, then `200` ticks (max `3` attempts).
3. Each retry performs cleanup + full rebuild to avoid stale objective/team collisions.
4. After max retries, log `ERROR`, keep player marked degraded, and rely on next periodic reconcile for re-entry.
5. Any successful render clears degraded state and pending retries immediately.

## Performance and Safety Rules

1. Sidebar updates must not run per tick.
2. Reconcile cadence must be `>=5s`.
3. Render operations remain idempotent for identical snapshots.
4. Data failures must not hide sidebar output.
5. Sidebar implementation is Bukkit/Paper-native only; no external sidebar library fallback.
6. Player-scoped scoreboard rendering may run after short join delay to avoid early lifecycle races.
7. Player reconcile loops stop promptly on quit/disable and must not leak stale retries.

## Assumptions

- Retry and periodic delays are scheduled through player-safe delayed scheduling.
- Bukkit/Paper scoreboard APIs may fail transiently during lifecycle races; deterministic retry/rebuild is the required resilience path.
