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

- `SchedulerBridge`: Folia-safe task abstraction for player/region/async/delayed execution
- `TeleportService`: command teleport policies, request lifecycle, stability delay, and RTP safety
- `SmpScoreboardService`: scoreboard lifecycle and periodic refresh orchestration
- `HotbarMenuService`: slot-8 menu token lifecycle and lock enforcement
