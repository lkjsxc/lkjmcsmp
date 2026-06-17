# Exchange Rates Contract

## Goal

Define strict per-item base rates with optional seasonal Cobblestone Point overrides controlled by explicit permissions.

## Base Rates

| Item | Cobblestone Points per Item |
| --- | --- |
| Oak Log | 16 |
| Spruce Log | 16 |
| Birch Log | 16 |
| Mysterious Egg | 10,000 |
| Home Slot 01 | 2,400 |
| Home Slot 02 | 3,120 |
| Home Slot 03 | 4,056 |
| Home Slot 04 | 5,272 |
| Home Slot 05 | 6,854 |
| Home Slot 06 | 8,911 |
| Home Slot 07 | 11,584 |
| Home Slot 08 | 15,059 |
| Home Slot 09 | 19,577 |
| Home Slot 10 | 25,450 |
| Home Slot 11 | 33,086 |
| Home Slot 12 | 43,011 |
| Home Slot 13 | 55,915 |
| Home Slot 14 | 72,690 |
| Home Slot 15 | 94,497 |
| Home Slot 16 | 122,846 |
| Home Slot 17 | 159,699 |
| Home Slot 18 | 207,609 |
| Home Slot 19 | 269,892 |
| Home Slot 20 | 350,860 |
| Home Slot 21 | 456,119 |
| Dirt | 1 |
| Sand | 2 |
| Gravel | 2 |
| End Stone | 8 |

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
12. Log purchases use per-item semantics (`1` log = `16` Cobblestone Points).
13. Quantity selection is not interpreted as multiplier units.
14. Service items (e.g., `temporary_dimension_pass`) deduct Cobblestone Points and trigger effects; no inventory capacity check is performed.
15. Service items must be purchased one at a time.
16. `home_slot_01` through `home_slot_21` are built-in fixed-price service items.
17. Home slot upgrades must be bought in key order; future-slot purchases are refunded and do not increase the limit.

## Item Keys and Display Names

1. The canonical config key for a shop item may differ from its player-facing display name.
2. `temporary_dimension_pass` is the config key; its display name is "Mysterious Egg".
3. `home_slot_01` through `home_slot_21` are built-in keys and remain available even when absent from `shop.yml`.
4. Display names are shown in menus; keys are used in commands and config files.

## Override Scope

- Overrides are server-global.
- Overrides persist across restarts.
- Overrides never mutate the base-rate document; they are runtime state.
- Overrides do not apply to built-in Home slot upgrade prices.
