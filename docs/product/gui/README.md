# GUI Contracts

This section defines menu hierarchy, button semantics, and interaction guarantees.

## Rules

1. All core systems must be reachable from GUI.
2. GUI labels and commands must stay synchronized.
3. GUI-denied actions return explicit reason feedback.
4. GUI actions use same permission checks as commands.

## Child Index

- [menu-tree.md](menu-tree.md): menu topology and entry points
- [interaction-rules.md](interaction-rules.md): click handling and consistency rules
