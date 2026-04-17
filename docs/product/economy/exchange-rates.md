# Exchange Rates Contract

## Goal

Define strict base rates with optional seasonal overrides controlled by explicit permissions.

## Base Rates

| Item | Quantity | Points Cost |
| --- | --- | --- |
| Oak Log | 1 | 16 |
| Spruce Log | 1 | 16 |
| Birch Log | 1 | 16 |
| Dirt | 64 | 48 |
| Sand | 64 | 72 |
| Gravel | 64 | 72 |

## Rules

1. Base rates are canonical defaults.
2. Seasonal override may modify cost and quantity per item.
3. Seasonal override changes require:
   - permission `lkjmcsmp.economy.override`
   - audit log record with actor, before, after, and timestamp
4. Shop purchase flow is quantity-driven: player selects integer unit count and total cost is `unit-cost * selected-units`.
5. Log purchases default to unit pricing (`1` log per unit, `16` points per unit).
6. Purchase fails when points are insufficient.
7. Purchase grants items only after points deduction succeeds.
8. Failed item insertion rolls back points deduction.

## Override Scope

- Overrides are server-global.
- Overrides persist across restarts.
- Overrides never mutate the base-rate document; they are runtime state.
