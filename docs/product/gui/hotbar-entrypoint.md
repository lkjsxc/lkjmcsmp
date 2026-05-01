# Hotbar Menu Entrypoint Contract

## Goal

Expose `/menu` through a deterministic right-end hotbar control.

## Slot Contract

1. Slot `8` (rightmost hotbar slot) is reserved for the menu item only when the player's setting enables it.
2. Slot `8` lock is hard-enforced while enabled: item replacement/removal attempts are cancelled.
3. Lock applies to click, drag, number-key swap, offhand swap, and inventory transfer vectors.
4. Player join/respawn must restore the menu item in slot `8` when enabled.
5. If slot `8` token is missing or tampered while enabled, it is reinstalled immediately.
6. If disabled, all plugin menu tokens are removed from the player's inventory and slot `8` becomes normal inventory space.

## Interaction Contract

1. Clicking/using the slot `8` menu item opens root menu.
2. Drop intent (`Q` / drop event) while targeting menu item is cancelled and opens root menu.
3. Clicking slot `8` while any inventory is open also opens root menu.
4. Slot `8` token interaction opens menu even when the underlying interaction is cancelled/blocked by reach or protection checks.
5. Slot `8` token interaction with entities follows the same open-menu behavior as block/air interaction.
6. Menu item interactions are silent on success (no extra spam messages).
7. Non-token item interactions never open the menu, even when slot `8` currently contains a valid token.
8. Tokens outside slot `8` are stale duplicates; interacting with them only cancels movement and schedules cleanup.
9. Inventory click handling may open the menu only for direct slot `8` token interaction.

## Failure Contract

1. If menu cannot open, player receives explicit failure reason.
2. Failure to open menu does not unlock or remove slot `8` reservation.
