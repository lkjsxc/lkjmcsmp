# GUI Contracts

## Goal

Define deterministic menu topology, navigation semantics, pagination behavior, and slot-8 entrypoint guarantees.

## Rules

1. Every core system is reachable from root menu.
2. GUI labels remain synchronized with command names and outcomes.
3. Locked or denied actions always explain why they are denied.
4. Menu entrypoint items remain hard-locked in their reserved positions.
5. Manual `Refresh` controls are shown only for picker menus and hidden elsewhere.
6. Primary action behavior must not depend on left-click vs right-click.
7. Destructive operations use explicit dedicated flows (page or confirm step), not alternate click types.
8. Menus refresh on state change only; automatic background reopen loops are disallowed.
9. Growth-heavy menus (homes, shop, progression, picker lists) use deterministic pagination.
10. Shop uses list-to-detail navigation for purchase execution.
11. Teleport accept UX mirrors `/tpaccept`: fail on none, accept directly on one, requester picker on many.
12. Team menu slot mapping follows the canonical layout contract.

## Child Index

- [menu-tree.md](menu-tree.md): canonical menu hierarchy and navigation controls
- [interaction-rules.md](interaction-rules.md): inventory click and feedback behavior
- [hotbar-entrypoint.md](hotbar-entrypoint.md): slot-8 menu token lock and interaction policy
- [team-layout.md](team-layout.md): team menu slot map and role-aware affordances
