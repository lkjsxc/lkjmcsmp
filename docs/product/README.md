# Product

## Goal

Define player-facing behavior for commands, menus, achievements, economy, social systems, and HUD output.

## Rules

1. Every player-visible behavior change updates this section first.
2. Command and GUI flows must describe the same outcomes.
3. Success/failure messaging expectations must remain explicit.
4. Growth-heavy surfaces should be specified with deterministic pagination contracts.
5. Destructive actions require explicit dedicated flows.
6. Shop quantity contracts use explicit final-item quantities (`1..64`) and avoid multiplier semantics.

## Child Index

- [commands/README.md](commands/README.md): command contracts and failure semantics
- [gui/README.md](gui/README.md): inventory menus, navigation, and interaction policy
- [economy/README.md](economy/README.md): points generation and exchange rules
- [achievements/README.md](achievements/README.md): achievement model and reward claim rules
- [hud/README.md](hud/README.md): action-bar HUD contracts and priority rules
- [social/README.md](social/README.md): party permissions and audit expectations
- [temporary-dimension/README.md](temporary-dimension/README.md): temporary dimension purchase and lifecycle
- [features/README.md](features/README.md): cross-cutting player-facing behaviors
