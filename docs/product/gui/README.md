# GUI Contracts

## Goal

Define deterministic menu topology, navigation semantics, and item-lock behavior.

## Rules

1. Every core system is reachable from root menu.
2. GUI labels remain synchronized with command names and outcomes.
3. Locked or denied actions always explain why they are denied.
4. Menu entrypoint items remain hard-locked in their reserved positions.

## Child Index

- [menu-tree.md](menu-tree.md): canonical menu hierarchy and navigation controls
- [interaction-rules.md](interaction-rules.md): inventory click and feedback behavior
- [hotbar-entrypoint.md](hotbar-entrypoint.md): slot-8 menu token lock and interaction policy
