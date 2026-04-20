# HUD Priority Contract

## Priority Order

1. Teleport status overlay (highest)
2. Combat target overlay
3. Idle HUD (default)

## Idle HUD Contract

1. Idle HUD renders:
   - `Points: <balance>`
   - `Online: <count>`
2. Idle HUD changes only when either source value changes.
3. Idle HUD must reclaim display immediately after higher-priority overlays expire.

## Overlay Arbitration Rules

1. A higher-priority overlay preempts lower-priority display immediately.
2. Lower-priority events that occur during preemption are not lost; latest state is rendered after preemption clears.
3. Overlay expiration is deterministic and player-scoped.
