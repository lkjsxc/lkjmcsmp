# Module Map

## Top-Level Packages

- `com.lkjmcsmp.plugin`: Bukkit/Folia entrypoint and wiring
- `com.lkjmcsmp.plugin.hud`: action-bar HUD state orchestration and listeners
- `com.lkjmcsmp.plugin.temporarydimension`: temporary dimension instance lifecycle and world management
- `com.lkjmcsmp.command`: command handlers and argument adapters
- `com.lkjmcsmp.gui`: inventory menu orchestration
- `com.lkjmcsmp.domain`: pure gameplay services and policies
- `com.lkjmcsmp.persistence`: SQLite repositories and mappers
- `com.lkjmcsmp.achievement`: achievement trackers and rewards

## Dependency Rules

1. `domain` does not depend on Bukkit APIs.
2. `command` and `gui` depend on `domain` interfaces.
3. `persistence` implements repository interfaces owned by `domain`.
4. `plugin` performs composition root responsibilities only.
5. HUD runtime components may depend on Bukkit/Paper APIs and `SchedulerBridge`, but HUD policy remains contract-driven from docs.

## Cross-Cutting Services

- `SchedulerBridge`: Folia-safe task abstraction for player/region/global/async/delayed execution
- `TeleportService`: command teleport policies, request lifecycle, stability delay, and RTP safety
- `ActionBarRouter`: facade entrypoint for player HUD updates and overlay arbitration
- `hud` runtime components: idle rendering, teleport/combat overlays, and periodic re-evaluation
- `HotbarMenuService`: slot-8 menu token lifecycle and lock enforcement
- `TemporaryDimensionManager`: registry, creation, expiry, evacuation, and cleanup of temporary dimension instances
- `InitialTriggerRtpListener`: repeatable configured-zone countdown RTP on join, respawn, teleport, world change, and movement
- `RespawnRtpListener`: overrides current-world-spawn death respawn with random teleport
- `StairSitService`: stair seat entity lifecycle and cleanup

## HUD Reliability Boundary

1. Runtime HUD contract must be satisfiable through Bukkit/Paper action-bar APIs.
2. No sidebar/packet fallback dependency may be introduced in runtime or verification paths.
3. HUD logs must include enough context (`trigger`, `playerUuid`, `state`) for operational triage.

## Cross-References

- [../folia/scheduler-contract.md](../folia/scheduler-contract.md): scheduling rules
- [../../product/hud/README.md](../../product/hud/README.md): player-facing HUD contracts
