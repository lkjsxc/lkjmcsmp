# Hotbar Menu Entrypoint Contract

## Goal

Expose `/menu` through a deterministic right-end hotbar control.

## Slot Contract

1. Slot `8` (rightmost hotbar slot) is reserved for the menu item.
2. Slot `8` lock is hard-enforced: item replacement/removal attempts are cancelled.
3. Lock applies to click, drag, number-key swap, offhand swap, and inventory transfer vectors.
4. Player join/respawn must restore the menu item in slot `8`.
5. If slot `8` token is missing or tampered, it is reinstalled immediately.

## Interaction Contract

1. Clicking/using the slot `8` menu item opens root menu.
2. Drop intent (`Q` / drop event) while targeting menu item is cancelled and opens root menu.
3. Clicking slot `8` while any inventory is open also opens root menu.
4. Slot `8` token interaction opens menu even when the underlying interaction is cancelled/blocked by reach or protection checks.
5. Slot `8` token interaction with entities follows the same open-menu behavior as block/air interaction.
6. Menu item interactions are silent on success (no extra spam messages).

## Failure Contract

1. If menu cannot open, player receives explicit failure reason.
2. Failure to open menu does not unlock or remove slot `8` reservation.
