# Action Bar HUD Contracts

## Goal

Define the player HUD contract now that sidebar scoreboard support is removed for Folia runtime reliability.

## Rules

1. Action bar is the only runtime HUD channel for persistent/ephemeral gameplay status.
2. HUD updates are state-change driven; no global periodic idle ticker.
3. HUD priority is deterministic across overlapping systems.
4. Teleport and combat overlays are temporary and must auto-expire.
5. Messages are scoped by `source`, carry an explicit priority, and may include a TTL.
6. The service suppresses redundant sends when the effective visible text has not changed.

## Child Index

- [hud-priority.md](hud-priority.md): priority order and idle/default content
- [teleport-combat-overlays.md](teleport-combat-overlays.md): temporary overlay contracts
