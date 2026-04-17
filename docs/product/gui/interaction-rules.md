# GUI Interaction Rules

## Input Rules

1. Left-click triggers primary action.
2. Right-click opens details where available.
3. Shift-click is ignored unless explicitly documented.
4. Closed inventories clear temporary session context.
5. Slot `8` hotbar menu item click/use opens root menu.
6. Slot `8` drop intent is cancelled and treated as menu-open input.
7. Clicking slot `8` while another inventory is open opens root menu.
8. Slot `8` hotbar token cannot be moved via drag, swap, number-key swap, offhand swap, or inventory transfer.

## Feedback Rules

1. Success sends concise confirmation message only after action completion.
2. Failure sends exact reason and required action.
3. Cooldown and timeout show numeric remaining time.
4. Permission failures include missing node.

## Consistency Rules

1. GUI action handlers delegate to same services as command handlers.
2. Menu pagination preserves stable ordering.
3. Disabled actions render with explicit locked tooltip.
4. Every menu has a deterministic back path to root.
5. Every plugin inventory view uses 54 slots.
6. Root menu exposes a deterministic close button.
7. Every non-root menu renders `Back` with `ARROW` material.
8. Menus hide manual `Refresh` controls by default.
9. Open menus auto-refresh every `1` second by default and after state-changing actions.
10. Shop quantity selection only changes previewed totals; purchase requires explicit `Buy`.
11. `/tpaccept`-linked accept flows open requester picker only when two or more requests are pending.
