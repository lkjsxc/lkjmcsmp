# HUD Priority Arbitration

## Summary

Deterministic priority ordering ensures that when multiple HUD sources compete, the player always sees the most important message.

## Priority Order

Priority enum values (lower ordinal = higher precedence):

1. `CRITICAL` — reserved for future server-wide alerts (currently unused)
2. `SYSTEM` — reserved for future maintenance messages (currently unused)
3. `TELEPORT`
4. `COMBAT`
5. `GAMEPLAY`
6. `INFO` — reserved for future info overlays (currently unused)
7. `IDLE` (lowest)

## Source Identifier Rules

1. Every action-bar request carries a `source` string key (for example `"teleport"`, `"combat"`, `"idle"`).
2. A new message with the same `source` replaces the previous message from that source.
3. Sources may optionally specify a `replacePolicy`; default is unconditional overwrite.

## Overlay Arbitration Rules

1. A higher-priority overlay preempts lower-priority display immediately.
2. Lower-priority events that occur during preemption are not lost; latest state is rendered after preemption clears.
3. Overlay expiration is deterministic and player-scoped.
4. Equal-priority ties break by newest message timestamp.
5. No redundant packet sends: the effective text must change before a new action-bar packet is emitted.
6. When the highest-priority overlay source is removed, the send-dedup key is cleared so idle text is re-emitted immediately.

## Cross-References

- [idle-guarantee.md](idle-guarantee.md): how idle reclaims display after overlays expire
- [renderer-contract.md](renderer-contract.md): dedup and packet dispatch details
