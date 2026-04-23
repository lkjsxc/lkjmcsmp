# Utility Command Contract

## Commands

- `/menu` opens root GUI.
- `/points` shows current points.
- `/convert cobblestone <amount>` converts cobblestone inventory to points.
- `/shop` opens points exchange GUI.
- `/achievement` opens the achievement GUI.
- `/ach` is a short alias for `/achievement`.
- `/achievement reset <player>` resets all achievement progress for a target player (admin only).

## Rules

1. `/menu` is always available to players with `lkjmcsmp.menu.use`.
2. Conversion consumes exact cobblestone count before granting points.
3. Negative and zero amounts are rejected.
4. Conversion amount may be capped by config per operation.
5. `/points` reflects committed ledger state, not temporary calculations.
6. Shop list opens item detail and displays the player's current points balance in-menu.
7. Shop detail provides direct purchase buttons for final quantity `1`, `2`, `4`, `8`, `16`, `32`, and `64`.
8. Shop detail has no separate "set quantity then buy" workflow.
9. Purchases use deterministic rate math (`points-per-item * quantity`).
10. Logs use per-item pricing by default (`1` log = `16` points).
11. Utility GUI views avoid background auto-refresh reopen loops.
12. Picker menus expose explicit manual `Refresh`.
13. Hotbar slot `8` provides an always-available menu entrypoint equivalent to `/menu`, including cancelled/blocked interaction contexts.
14. Achievement claims from GUI and `/achievement claim <key>` use the same service path.
15. `/achievement reset <player>` requires `lkjmcsmp.achievement.admin` and deletes all achievement state for the target.

## Failures

- Insufficient cobblestone: reject with missing amount.
- Overflow/invalid amount: reject and log.
- Exchange without sufficient points: reject with deficit.
- Unknown achievement key: reject with explicit key-missing result.
