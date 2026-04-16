# Exchange Rates Contract

## Goal

Define strict base rates with optional seasonal overrides controlled by explicit permissions.

## Base Rates

| Item | Quantity | Points Cost |
| --- | --- | --- |
| Oak Log | 16 | 96 |
| Spruce Log | 16 | 96 |
| Birch Log | 16 | 96 |
| Dirt | 64 | 48 |
| Sand | 64 | 72 |
| Gravel | 64 | 72 |

## Rules

1. Base rates are canonical defaults.
2. Seasonal override may modify cost and quantity per item.
3. Seasonal override changes require:
   - permission `lkjmcsmp.economy.override`
   - audit log record with actor, before, after, and timestamp
4. Purchase fails when points are insufficient.
5. Purchase grants items only after points deduction succeeds.
6. Failed item insertion rolls back points deduction.

## Override Scope

- Overrides are server-global.
- Overrides persist across restarts.
- Overrides never mutate the base-rate document; they are runtime state.
