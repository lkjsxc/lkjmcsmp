# GUI Contracts

## Goal

Define deterministic menu topology, navigation semantics, pagination behavior, slot-8 entrypoint guarantees, and visual design system.

## Rules

1. Every core system is reachable from root menu.
2. GUI labels remain synchronized with command names and outcomes.
3. Locked or denied actions always explain why they are denied.
4. Menu entrypoint items remain hard-locked only for players whose setting enables the hotbar item.
5. Manual `Refresh` controls are shown only for picker menus and hidden elsewhere.
6. Primary action behavior must not depend on left-click vs right-click.
7. Destructive operations use explicit dedicated flows (page or confirm step), not alternate click types.
8. Menus refresh on state change only; automatic background reopen loops are disallowed.
9. Growth-heavy menus (homes, shop, achievement, picker lists) use deterministic pagination.
10. Shop uses list-to-detail navigation for purchase execution.
11. Shop detail purchases are direct-buy quantity actions (`1`, `2`, `4`, `8`, `16`, `32`, `64`).
12. Team disband from menu requires dedicated confirm screen.
13. Teleport accept UX mirrors `/tpaccept`: fail on none, accept directly on one, requester picker on many.
14. All menus follow canonical slot maps; action placement must stay deterministic across releases.
15. Team menu slot mapping follows the canonical layout contract.
16. Every plugin menu uses decorative borders for visual hierarchy and consistency.
17. Service-type shop items (world effects, not inventory items) follow the same list-to-detail flow but execute effects on purchase.
18. Service items in shop lists display their configured material and `"§dService Item — executes on purchase"` lore line.
19. Decorative panes, info panels, page indicators, selected summaries, and balance indicators are inert and never emit unknown-action chat.
20. The Settings page is reachable from root menu and owns player preference toggles.
21. Player-facing labels and messages are rendered through the selected language where localized text exists.

## Visual Design System

### Border Contract

1. Every 54-slot menu renders a decorative border using stained glass panes.
2. Border slots: top row `0..8`, bottom row `45..53` (excluding functional controls), left column `9,18,27,36`, right column `17,26,35,44`.
3. Each menu category has a canonical border color:
   - Root: `LIGHT_BLUE_STAINED_GLASS_PANE`
   - Shop: `YELLOW_STAINED_GLASS_PANE`
   - Teleport: `PURPLE_STAINED_GLASS_PANE`
   - Homes: `RED_STAINED_GLASS_PANE`
   - Warps: `GREEN_STAINED_GLASS_PANE`
   - Team: `CYAN_STAINED_GLASS_PANE`
   - Achievement: `ORANGE_STAINED_GLASS_PANE`
   - Profile: `LIME_STAINED_GLASS_PANE`
   - Picker: `LIGHT_GRAY_STAINED_GLASS_PANE`
4. Functional slots within border rows/columns take precedence over decorative items.
5. Empty content slots inside the border remain empty (no filler) unless used for info panels.

### Info Panel Contract

1. Slot `4` is reserved for a contextual info panel on menus that benefit from a header.
2. Info panels display concise category-relevant text (e.g., current points balance in shop menus).
3. Info panels use `PAPER` material with gray or gold display names.

## Child Index

- [menu-tree.md](menu-tree.md): canonical menu hierarchy and navigation controls
- [slot-maps.md](slot-maps.md): canonical slot map for every menu surface
- [interaction-rules.md](interaction-rules.md): inventory click and feedback behavior
- [shop-actions.md](shop-actions.md): shop list, detail, and cobblestone conversion actions
- [hotbar-entrypoint.md](hotbar-entrypoint.md): slot-8 menu token lock and interaction policy
- [inventory-sync.md](inventory-sync.md): client/server hotbar and pickup synchronization
- [team-layout.md](team-layout.md): team menu slot map and role-aware affordances
