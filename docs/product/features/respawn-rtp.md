# Respawn Random Teleport Contract

## Goal

When a player dies and would respawn at the world's initial spawn point, randomly teleport them instead to spread players across the map.

## Rules

1. Triggered on `PlayerRespawnEvent`.
2. Applies only when the respawn reason is `DEFAULT_SPAWN` (the world's initial spawn point).
3. Does not apply to bed respawns, anchor respawns, or any non-default spawn.
4. Uses `TeleportService.randomTeleport` with `bypassCooldown=true` and `applyStabilityDelay=false`.
5. Requires permission `lkjmcsmp.rtp.use`.
6. Config key `respawn-on-death.random-teleport.enabled` defaults to `true`.

## Failure Contract

1. If no safe RTP location is found, the player remains at the world spawn and receives an explicit failure message.
2. If the player lacks `lkjmcsmp.rtp.use`, they respawn normally at the world spawn.

## Cross-References

- [../commands/teleport.md](../commands/teleport.md): RTP rules and safety validation
