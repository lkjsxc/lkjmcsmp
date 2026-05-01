# Respawn Random Teleport Contract

## Goal

When a player dies and would respawn at the world's current spawn point, randomly teleport them instead to spread players across the map.

## Rules

1. Triggered on `PlayerRespawnEvent`.
2. Applies only when the respawn reason is `PlayerRespawnEvent.RespawnReason.DEATH` and the final respawn block equals the target world's current spawn block.
3. Does not apply to bed respawns, anchor respawns, `END_PORTAL` exits, temporary-dimension death returns, or other plugin-set respawns.
4. Chooses a safe random location during the respawn event and sets it with `PlayerRespawnEvent#setRespawnLocation`.
5. Requires permission `lkjmcsmp.rtp.use`.
6. Config key `respawn-on-death.random-teleport.enabled` defaults to `true`.
7. The comparison uses `World#getSpawnLocation()` at respawn time so operator or plugin spawn changes are honored.
8. The player must not visibly respawn at the spawn block before the random location is applied.

## Failure Contract

1. If no safe RTP location is found, the player remains at the world spawn and receives an explicit failure message.
2. If the player lacks `lkjmcsmp.rtp.use`, they respawn normally at the world spawn.

## Cross-References

- [../commands/teleport.md](../commands/teleport.md): RTP rules and safety validation
