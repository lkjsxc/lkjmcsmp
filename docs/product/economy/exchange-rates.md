# Exchange Rates Contract

## Goal

Define strict per-item base rates with optional seasonal Cobblestone Point overrides controlled by explicit permissions.

## Base Rates

| Item | Cobblestone Points per Item |
| --- | --- |
| Oak Log | 8 |
| Spruce Log | 8 |
| Birch Log | 8 |
| Mysterious Egg | 10,000 |
| Dirt | 1 |
| Sand | 2 |
| Gravel | 2 |
| End Stone | 8 |

## Home Upgrade Rates

Home slots are bought from the Homes surface, not from the Points Shop.

| Upgrade | Cobblestone Points |
| --- | --- |
| Home Slot 01 | 600 |
| Home Slot 02 | 780 |
| Home Slot 03 | 1,014 |
| Home Slot 04 | 1,318 |
| Home Slot 05 | 1,713 |
| Home Slot 06 | 2,228 |
| Home Slot 07 | 2,896 |
| Home Slot 08 | 3,765 |
| Home Slot 09 | 4,894 |
| Home Slot 10 | 6,363 |
| Home Slot 11 | 8,271 |
| Home Slot 12 | 10,753 |
| Home Slot 13 | 13,979 |
| Home Slot 14 | 18,173 |
| Home Slot 15 | 23,624 |
| Home Slot 16 | 30,712 |
| Home Slot 17 | 39,925 |
| Home Slot 18 | 51,902 |
| Home Slot 19 | 67,473 |
| Home Slot 20 | 87,715 |
| Home Slot 21 | 114,030 |

## Rules

1. Base rates are canonical defaults.
2. Seasonal override may modify Cobblestone Points per item.
3. Seasonal override changes require:
   - permission `lkjmcsmp.economy.override`
   - audit log record with actor, before, after, and timestamp
4. Shop purchase flow is list-to-detail:
   - list view selects target item
   - detail view controls final item quantity (`1..64`)
   - detail view executes explicit `Buy`
5. Opening detail view resets quantity to default baseline (`1` item).
6. Total cost is `points-per-item * selected-quantity`.
7. Purchase succeeds only when Cobblestone Points balance and inventory capacity are sufficient.
8. Purchase grants items or executes service effects only after Cobblestone Points deduction succeeds.
9. Failed service effects are refunded by the service purchase flow.
10. Temporary dimension creation failures are refunded automatically with reason `TEMPORARY_DIMENSION_REFUND`.
11. Non-temporary-dimension service failures are refunded with reason `SERVICE_PURCHASE_REFUND`.
12. Log purchases use per-item semantics (`1` log = `8` Cobblestone Points).
13. Quantity selection is not interpreted as multiplier units.
14. Service shop items (e.g., `temporary_dimension_pass`) deduct Cobblestone Points and trigger effects; no inventory capacity check is performed.
15. Service items must be purchased one at a time.
16. Home slot upgrades are not shop items and never appear in the Points Shop list.
17. Home slot upgrades must be bought in order; future-slot requests fail before deduction.

## Item Keys and Display Names

1. The canonical config key for a shop item may differ from its player-facing display name.
2. `temporary_dimension_pass` is the config key; its display name is "Mysterious Egg".
3. `home_slot_01` through `home_slot_21` are Home upgrade keys, not shop config keys.
4. Display names are shown in menus; keys are used in commands and config files.

## Override Scope

- Overrides are server-global.
- Overrides persist across restarts.
- Overrides never mutate the base-rate document; they are runtime state.
- Overrides do not apply to Home slot upgrade prices.
