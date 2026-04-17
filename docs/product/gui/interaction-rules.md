# GUI Interaction Rules

## Input Rules

1. Item click behavior is action-stable across left/right click.
2. Shift-click is ignored unless explicitly documented.
3. Closed inventories clear temporary session context.
4. Slot `8` hotbar menu item click/use opens root menu.
5. Slot `8` drop intent is cancelled and treated as menu-open input.
6. Clicking slot `8` while another inventory is open opens root menu.
7. Slot `8` hotbar token cannot be moved via drag, swap, number-key swap, offhand swap, or inventory transfer.
8. Destructive actions (for example home deletion) are reachable only through explicit dedicated views.

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
8. Menus refresh immediately after state-changing actions.
9. Automatic background refresh reopen loops are disallowed.
10. Picker menus expose explicit manual `Refresh`; non-picker menus do not.
11. Shop quantity selection uses final item counts (`1..64`), updates preview totals only, and requires explicit `Buy`.
12. `/tpaccept`-linked accept flows open requester picker only when two or more requests are pending.
