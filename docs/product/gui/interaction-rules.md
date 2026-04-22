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

## Service Item Rules

1. Service-type shop items trigger world or player effects rather than giving inventory items.
2. Service items follow the same list-to-detail navigation as physical items.
3. Service item detail menus show the same direct-buy quantity buttons.
4. On purchase, service effects execute only after point deduction succeeds.
5. Service purchase failure rolls back any partial effect execution.
6. The `temporary_end` service item creates a temporary End dimension instance and transfers nearby players.

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
10. Picker menus expose explicit manual `Refresh`.
11. Shop detail uses direct-buy final quantities (`1`, `2`, `4`, `8`, `16`, `32`, `64`) with no separate buy button.
12. Team disband from GUI requires explicit confirm screen before execution.
13. `/tpaccept`-linked accept flows open requester picker when 2+ pending requests exist.
