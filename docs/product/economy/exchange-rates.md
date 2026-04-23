# Exchange Rates Contract

## Goal

Define strict per-item base rates with optional seasonal Maruishi Point overrides controlled by explicit permissions.

## Base Rates

| Item | Maruishi Points per Item |
| --- | --- |
| Oak Log | 16 |
| Spruce Log | 16 |
| Birch Log | 16 |
| Dirt | 1 |
| Sand | 2 |
| Gravel | 2 |
| Temporary End Pass | 10,000 |

## Rules

1. Base rates are canonical defaults.
2. Seasonal override may modify Maruishi Points per item.
3. Seasonal override changes require:
   - permission `lkjmcsmp.economy.override`
   - audit log record with actor, before, after, and timestamp
4. Shop purchase flow is list-to-detail:
   - list view selects target item
   - detail view controls final item quantity (`1..64`)
   - detail view executes explicit `Buy`
5. Opening detail view resets quantity to default baseline (`1` item).
6. Total cost is `points-per-item * selected-quantity`.
7. Purchase succeeds only when Maruishi Points balance and inventory capacity are sufficient.
8. Purchase grants items only after Maruishi Points deduction succeeds.
9. Failed item insertion rolls back Maruishi Points deduction.
10. Log purchases use per-item semantics (`1` log = `16` Maruishi Points).
11. Quantity selection is not interpreted as multiplier units.
12. Service items (e.g., `temporary_end_pass`) deduct Maruishi Points and trigger effects; no inventory capacity check is performed.

## Item Keys and Display Names

1. The canonical config key for a shop item may differ from its player-facing display name.
2. `temporary_end_pass` is the config key; its display name is "Temporary End Pass".
3. Display names are shown in menus; keys are used in commands and config files.

## Override Scope

- Overrides are server-global.
- Overrides persist across restarts.
- Overrides never mutate the base-rate document; they are runtime state.
