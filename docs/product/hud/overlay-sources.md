# HUD Overlay Sources

## Summary

Each gameplay system that needs temporary action-bar visibility follows the same overlay contract: store a message with a priority and TTL, and let the periodic evaluator manage visibility.

## Teleport Overlay

1. During stability delay, action bar displays countdown seconds remaining.
2. Countdown updates are state-change based (each second boundary).
3. On completion, action bar emits explicit success text.
4. On cancellation or failure, action bar emits explicit failure text.
5. Chat messaging remains enabled in parallel.
6. Source identifier: `"teleport"`, priority: `TELEPORT`.
7. TTL expiry is logical: the message carries `expiresAt` and is evicted during periodic evaluation.

## Combat Overlay

1. Trigger: player damages a living target (mob or player).
2. Content: target name plus two-tone color HP bar only. The literal text `"HP"` must not appear.
3. Format: `§e<targetName>§f <hpBar>`
4. Duration: `3` seconds after latest qualifying hit by that player.
5. Re-hit during active overlay refreshes TTL and updates bar fill.
6. Source identifier: `"combat"`, priority: `COMBAT`.
7. Post-damage health is used where available.
8. Offline, dead, or invalid target states collapse safely back to lower-priority HUD.

## Gameplay Overlays

1. Shop purchase confirmation: `GAMEPLAY` priority, `3`-second TTL, source `"shop"`.
2. Temporary dimension countdown while inside a temporary dimension world: `GAMEPLAY` priority, `30`-second refresh, source `"tempdim"`.

## Safety Rules

1. Combat overlay never suppresses teleport overlay.
2. Overlay calculations use post-damage health where available.
3. Expired messages are evicted lazily during periodic evaluation.
4. After any overlay expires, the idle message is guaranteed to render within the same tick if no other overlay remains.

## Cross-References

- [priority-arbitration.md](priority-arbitration.md): how overlays compete
- [renderer-contract.md](renderer-contract.md): eviction and re-render timing
