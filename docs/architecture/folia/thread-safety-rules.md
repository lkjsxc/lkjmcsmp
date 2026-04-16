# Thread Safety Rules

## Bukkit/Folia Boundary

1. Only adapter layer touches Bukkit entities, worlds, and inventories.
2. Domain services consume immutable value objects.
3. Repository calls never run inside inventory click listeners without async handoff.

## State Rules

1. In-memory request caches use thread-safe collections.
2. Expiration sweeps run on global scheduler tasks.
3. Shared mutable maps are encapsulated behind service methods.

## Error Rules

1. Scheduler bridge failures surface explicit command and GUI errors.
2. Cross-thread access violations are logged with operation context.
