# Utility Command Contract

## Commands

- `/menu` opens root GUI.
- `/points` shows current points.
- `/convert cobblestone <amount>` converts cobblestone inventory to points.
- `/shop` opens points exchange GUI.
- `/adv` opens pseudo-advancement GUI.

## Rules

1. `/menu` is always available to players with `lkjmcsmp.menu.use`.
2. Conversion consumes exact cobblestone count before granting points.
3. Negative and zero amounts are rejected.
4. Conversion amount may be capped by config per operation.
5. `/points` reflects committed ledger state, not temporary calculations.
6. GUI points shop includes a convert action that converts all available cobblestone by default.
7. Hotbar slot `8` provides an always-available menu entrypoint equivalent to `/menu`.

## Failures

- Insufficient cobblestone: reject with missing amount.
- Overflow/invalid amount: reject and log.
- Exchange without sufficient points: reject with deficit.
