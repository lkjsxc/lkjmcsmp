# HUD Renderer Contract

## Summary

A per-player periodic task evaluates HUD state, evicts expired messages, and dispatches action-bar packets often enough that the client action bar never fades out.

## Per-Player Periodic Task

1. Every online player has a dedicated periodic re-evaluation task scheduled on their player scheduler.
2. Task interval: `2` ticks (`0.1` second).
3. Each tick performs:
   - Evict expired messages from the player's state.
   - Compute the effective message by priority and timestamp.
   - If effective is null, synthesize a fallback idle string.
   - Dispatch `player.sendActionBar(text)` in player-safe context every evaluation.
4. On player join, the periodic task starts immediately.
5. On player quit, the periodic task stops and state is dropped.
6. On plugin disable, all periodic tasks stop.

## Continuous Send Rule

1. The renderer sends the effective action-bar text every `2` ticks, even when the text is unchanged.
2. This intentionally avoids relying on client-side action-bar retention.
3. State may still remember the last text for diagnostics, but it must not suppress periodic sends.

## Immediate Trigger

1. When an overlay is added or updated, an immediate re-evaluation may be triggered in addition to the periodic task.
2. Immediate triggers do not bypass deduplication; they simply move the next evaluation to the current tick.

## Idle Fallback

1. If state contains no idle source, the composer synthesizes `Playtime: 0h 0m | Online: <count>`.
2. This is a safety net; normal operation should always have an idle source present.

## Cross-References

- [priority-arbitration.md](priority-arbitration.md): priority computation
- [idle-guarantee.md](idle-guarantee.md): idle reclaim behavior
- [overlay-sources.md](overlay-sources.md): overlay TTL semantics
