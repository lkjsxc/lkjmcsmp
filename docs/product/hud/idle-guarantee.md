# Idle HUD Guarantee

## Summary

The idle message is a permanent background layer. It is always present in state and automatically becomes visible whenever no overlay is active.

## Idle Content

1. Idle HUD renders: `Cobblestone Points: <balance> | Online: <count>`
2. Idle HUD changes only when either source value changes.
3. Idle HUD must reclaim display immediately after higher-priority overlays expire.
4. **Always-On Idle Guarantee**: the action bar must never be blank for an online player. Idle is stored as a persistent source with `expiresAt = -1`.
5. Idle message uses source `"idle"` and priority `IDLE`.

## Refresh Rules

1. Idle content refreshes every `20` seconds for all online players to keep values accurate. The per-player periodic renderer sends action-bar packets every `2` ticks to prevent client fade-out.
2. Refresh updates the idle source in the player's state directly; the next periodic evaluation picks it up.
3. On player join, idle is computed and injected immediately before the first periodic tick.

## Reclaim Rules

1. When an overlay expires or is explicitly removed, idle becomes the effective message on the next periodic evaluation.
2. Continuous rendering ensures idle is sent immediately even if the text happens to match the last visible value.
3. If no idle message exists in state (an illegal condition), the composer must synthesize a fallback idle string before rendering.

## Cross-References

- [priority-arbitration.md](priority-arbitration.md): how idle competes with overlays
- [renderer-contract.md](renderer-contract.md): dedup clearing on overlay removal
