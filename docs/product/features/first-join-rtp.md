# First-Join Random Teleport Contract

## Goal

When a player joins the server for the first time, randomly teleport them to spread new players across the map.

## Rules

1. Triggered on `PlayerJoinEvent`.
2. Applies only when the player has no prior join record in the database.
3. Uses `TeleportService.randomTeleport` with `bypassCooldown=true` and `applyStabilityDelay=false`.
4. Target world is configurable under `teleport.first-join.world`.
5. Config key `teleport.first-join.enabled` defaults to `true`.

## Failure Contract

1. If no safe RTP location is found, the player remains at the default spawn and receives an explicit failure message.
2. If the configured world is empty or invalid, the player remains at the default spawn.

## Cross-References

- [../commands/teleport.md](../commands/teleport.md): RTP rules and safety validation
- [respawn-rtp.md](respawn-rtp.md): similar contract for respawn random teleport
