# Point Model

## Goal

Provide a strict, auditable Maruishi Points economy where cobblestone conversion is the primary source.

## Core Rule

- `1 cobblestone = 1 Maruishi Point`

## Conversion Rules

1. Conversion consumes physical cobblestone from player inventory.
2. Conversion is atomic: consume and grant Maruishi Points in one transaction.
3. Partial consume is allowed when requested amount exceeds inventory, only if `allowPartial=true` in config.
4. Default behavior rejects when requested amount exceeds inventory.

## Ledger Rules

1. Ledger records every mutation with reason code.
2. Balance cannot go negative.
3. Operator adjustments require permission and are audit logged.
4. Seasonal rate overrides are independently logged with actor and timestamp.

## Reason Codes

- `COBBLE_CONVERT`
- `SHOP_PURCHASE`
- `ADMIN_ADJUST`
- `SEASONAL_OVERRIDE_APPLIED`
- `TEMPORARY_END_REFUND`
