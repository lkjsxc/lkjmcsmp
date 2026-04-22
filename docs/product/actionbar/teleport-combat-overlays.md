# Teleport and Combat Overlay Contract

## Teleport Overlay

1. During stability delay, action bar displays countdown seconds remaining.
2. Countdown updates are state-change based (each second boundary), not global periodic polling.
3. On completion, action bar emits explicit success text.
4. On cancellation/failure, action bar emits explicit failure text.
5. Chat messaging remains enabled in parallel.
6. Teleport overlay uses source identifier `"teleport"` and priority `TELEPORT`.
7. TTL expiry schedules a player-scoped delayed cleanup task.

## Combat Overlay

1. Trigger: player damages a living target (mob or player).
2. Content: target name plus two-tone color HP bar only. The literal text `"HP"` must not appear.
3. Format: `§e<targetName>§f <hpBar>`
4. Duration: `3` seconds after latest qualifying hit by that player.
5. Re-hit during active overlay refreshes TTL and updates bar fill.
6. Combat overlay uses source identifier `"combat"` and priority `COMBAT`.
7. TTL expiry schedules a player-scoped delayed cleanup task.

## Safety Rules

1. Combat overlay never suppresses teleport overlay.
2. Overlay calculations use post-damage health where available.
3. Offline/dead/invalid target states collapse safely back to lower-priority HUD.
4. Expired messages are evicted lazily during render and by scheduled cleanup.
5. After any overlay expires, the idle message is guaranteed to render within the same tick if no other overlay remains.
