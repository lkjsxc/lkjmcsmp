# HUD Renderer Contract

## Summary

A per-player periodic task evaluates HUD state, evicts expired messages, and dispatches action-bar packets with strict deduplication.

## Per-Player Periodic Task

1. Every online player has a dedicated periodic re-evaluation task scheduled on their player scheduler.
2. Task interval: `2` ticks (`0.1` second).
3. Each tick performs:
   - Evict expired messages from the player's state.
   - Compute the effective message by priority and timestamp.
   - If effective is null, synthesize a fallback idle string.
   - Compare effective text against `lastSent`; send only if changed.
   - Dispatch `player.sendActionBar(text)` in player-safe context.
4. On player join, the periodic task starts immediately.
5. On player quit, the periodic task stops and state is dropped.
6. On plugin disable, all periodic tasks stop.

## Deduplication

1. `lastSent` stores the most recently emitted action-bar text per player.
2. If computed effective text equals `lastSent`, no packet is sent.
3. When the highest-priority overlay is removed from state, `lastSent` is cleared so the next evaluation always sends.

## Immediate Trigger

1. When an overlay is added or updated, an immediate re-evaluation may be triggered in addition to the periodic task.
2. Immediate triggers do not bypass deduplication; they simply move the next evaluation to the current tick.

## Idle Fallback

1. If state contains no idle source, the composer synthesizes `Cobblestone Points: 0 | Online: <count>`.
2. This is a safety net; normal operation should always have an idle source present.

## Cross-References

- [priority-arbitration.md](priority-arbitration.md): priority computation
- [idle-guarantee.md](idle-guarantee.md): idle reclaim behavior
- [overlay-sources.md](overlay-sources.md): overlay TTL semantics
