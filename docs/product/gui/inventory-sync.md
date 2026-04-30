# Inventory Synchronization Contract

## Summary

Plugin-owned inventory state must be corrected both server-side and client-side after events that commonly create ghost items or stale hotbar views.

## Hotbar Token Sync

1. Slot `8` is the only valid location for the menu token.
2. If the token appears on the cursor or outside slot `8`, it is removed.
3. If slot `8` is missing the token, the token is reinstalled.
4. After correction, `Player#updateInventory()` is called from the player scheduler.
5. Sync runs immediately and again after a short delay when the event is known to race with vanilla inventory updates.
6. Respawn sync force-rewrites slot `8` over multiple delayed passes so the post-respawn client inventory handshake cannot hide the token.

## Sync Triggers

1. Player join.
2. Player respawn, with forced delayed retries at `1`, `2`, `5`, `10`, `20`, and `40` ticks.
3. Inventory close after a plugin menu.
4. Cancelled token click, drag, swap, drop, or offhand interaction.
5. Player item pickup.
6. Any explicit token repair path invoked by menu open.

## Pickup Rule

1. Item pickup must not move or duplicate the slot `8` token.
2. Pickup completion schedules a token sync on the player scheduler.
3. Sync must not cancel normal pickup unless the picked item itself is a token.

## Accidental Menu Open Rule

1. A hotbar or inventory click opens the menu only when the actual token item is used.
2. Clicking slot `8` without a valid token cancels the mutation and repairs the token, but does not open the menu.
3. Number-key interactions open the menu only when the source or destination item is the token.
4. Non-token slot `4` or slot `5` interactions never open the menu.

## Cross-References

- [hotbar-entrypoint.md](hotbar-entrypoint.md): reserved slot and interaction policy
- [interaction-rules.md](interaction-rules.md): general click behavior
