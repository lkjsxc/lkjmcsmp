# Scoreboard Render Architecture

## Goal

Define a scratch-style, deterministic architecture for Folia-safe sidebar rendering.

## Component Model

1. **Lifecycle Coordinator**
   - Owns start/stop, join/quit hooks, targeted refresh entrypoint, and per-player reconcile loop scheduling.
2. **Player Tracking State**
   - Owns tracked player IDs, per-player render epoch, degraded player set, and online-count aggregate.
3. **Snapshot Loader**
   - Loads `points` asynchronously and merges with tracked online count into immutable render snapshot.
4. **Sidebar Renderer**
   - Ensures board primitives, applies canonical title/lines, reasserts `DisplaySlot.SIDEBAR`, and verifies contract.
5. **Recovery Coordinator**
   - Handles render failure classification, retry scheduling, cleanup-first retries, and degraded/recovered transitions.
6. **Teardown Cleaner**
   - Resets player board to main scoreboard and clears all per-player in-memory scoreboard lifecycle state.

## Snapshot Contract

Snapshot fields are immutable values consumed by a single render attempt:

1. `playerUuid`
2. `trigger`
3. `attempt`
4. `epoch`
5. `onlineCount`
6. `points` (fallback `0` when lookup fails)

## Render Pipeline

1. Build immutable snapshot off player thread.
2. Enter player-safe mutation context for target player.
3. Optionally cleanup player board first (retry/rebuild path).
4. Ensure managed objective exists using fixed identity from layout contract.
5. Write canonical lines in canonical order.
6. Set and verify `DisplaySlot.SIDEBAR` ownership.
7. Verify required objective and line score ordering.
8. On success, clear degraded/retry state for the player.

## Identity Rules

1. Managed objective identity is fixed and reused.
2. Visible label text is contract-driven and deterministic.
3. Internal entry identities are stable across render attempts.
4. Retry/reconcile paths never switch to alternate objective names.

## State Isolation Rules

1. Per-player render epochs prevent stale delayed tasks from mutating live state.
2. Retry scheduling and periodic scheduling both validate epoch freshness before work.
3. Player quit/remove immediately invalidates future mutations for that player.
4. Shared aggregates (`onlineCount`) are copied into snapshots before render dispatch.
