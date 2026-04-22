# HUD Priority Contract

## Priority Order

Priority enum values (lower ordinal = higher precedence):

1. `CRITICAL`
2. `SYSTEM`
3. `TELEPORT`
4. `COMBAT`
5. `GAMEPLAY`
6. `INFO`
7. `IDLE` (lowest)

## Idle HUD Contract

1. Idle HUD renders:
   - `Points: <balance>`
   - `Online: <count>`
2. Idle HUD changes only when either source value changes.
3. Idle HUD must reclaim display immediately after higher-priority overlays expire.
4. **Always-On Idle Guarantee**: the action bar must never be blank for an online player. If no overlay is active and no idle message is in state, the service must inject a fallback idle message immediately before rendering.
5. Idle message uses `expiresAt = -1` (never expires) and source `"idle"`.

## Overlay Arbitration Rules

1. A higher-priority overlay preempts lower-priority display immediately.
2. Lower-priority events that occur during preemption are not lost; latest state is rendered after preemption clears.
3. Overlay expiration is deterministic and player-scoped.
4. Equal-priority ties break by newest message timestamp.
5. No redundant packet sends: the effective text must change before a new action-bar packet is emitted.
6. When an overlay expires and no other overlay remains, idle text is re-evaluated and sent even if it was previously sent, ensuring the action bar never stays blank.

## Source Identifier Rules

1. Every action-bar request carries a `source` string key (for example `"teleport"`, `"combat"`, `"idle"`).
2. A new message with the same `source` replaces the previous message from that source.
3. Sources may optionally specify a `replacePolicy`; default is unconditional overwrite.
