# Teleport and Combat Overlay Contract

## Teleport Overlay

1. During stability delay, action bar displays countdown seconds remaining.
2. Countdown updates are state-change based (each second boundary), not global periodic polling.
3. On completion, action bar emits explicit success text.
4. On cancellation/failure, action bar emits explicit failure text.
5. Chat messaging remains enabled in parallel.

## Combat Overlay

1. Trigger: player damages a living target (mob or player).
2. Content: target name plus two-tone color HP bar only (no numeric health).
3. Duration: `3` seconds after latest qualifying hit by that player.
4. Re-hit during active overlay refreshes TTL and updates bar fill.

## Safety Rules

1. Combat overlay never suppresses teleport overlay.
2. Overlay calculations use post-damage health where available.
3. Offline/dead/invalid target states collapse safely back to lower-priority HUD.
