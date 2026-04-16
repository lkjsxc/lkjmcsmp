# Module Map

## Top-Level Packages

- `com.lkjmcsmp.plugin`: Bukkit/Folia entrypoint and wiring
- `com.lkjmcsmp.command`: command handlers and argument adapters
- `com.lkjmcsmp.gui`: inventory menu orchestration
- `com.lkjmcsmp.domain`: pure gameplay services and policies
- `com.lkjmcsmp.persistence`: SQLite repositories and mappers
- `com.lkjmcsmp.progression`: pseudo-advancement trackers and rewards
- `com.lkjmcsmp.verify`: internal verification helpers for smoke checks

## Dependency Rules

1. `domain` does not depend on Bukkit APIs.
2. `command` and `gui` depend on `domain` interfaces.
3. `persistence` implements repository interfaces owned by `domain`.
4. `plugin` performs composition root responsibilities only.

## Cross-Cutting Services

- `ClockService`
- `AuditService`
- `PermissionService`
- `SchedulerBridge` (Folia-safe task abstraction)
