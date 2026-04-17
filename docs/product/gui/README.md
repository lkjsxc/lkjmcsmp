# GUI Contracts

## Goal

Define deterministic menu topology, navigation semantics, and item-lock behavior.

## Rules

1. Every core system is reachable from root menu.
2. GUI labels remain synchronized with command names and outcomes.
3. Locked or denied actions always explain why they are denied.
4. Menu entrypoint items remain hard-locked in their reserved positions.
5. Manual `Refresh` controls are hidden by default in production menus.
6. Open menus auto-refresh every `1` second by default and immediately after state changes.
7. Teleport accept UX mirrors `/tpaccept`: fail on none, accept directly on one, requester picker on many.

## Child Index

- [menu-tree.md](menu-tree.md): canonical menu hierarchy and navigation controls
- [interaction-rules.md](interaction-rules.md): inventory click and feedback behavior
- [hotbar-entrypoint.md](hotbar-entrypoint.md): slot-8 menu token lock and interaction policy
