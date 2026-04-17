# Thread Safety Rules

## Bukkit/Folia Boundary

1. Only adapter layer touches Bukkit entities, worlds, and inventories.
2. Domain services consume immutable value objects.
3. Repository calls never run inside inventory click listeners without async handoff.
4. Never read another player's live location from the wrong player thread.
5. World/chunk block-state reads for RTP probing run in region-safe tasks.
6. Scoreboard player mutation tasks never scan online players; cross-player aggregates are precomputed from tracked state.

## State Rules

1. In-memory request caches use thread-safe collections.
2. Expiration sweeps run on global scheduler tasks.
3. Shared mutable maps are encapsulated behind service methods.

## Error Rules

1. Scheduler bridge failures surface explicit command and GUI errors.
2. Cross-thread access violations are logged with operation context.
3. Teleport completion failures are reported as failures, never as successful teleports.
