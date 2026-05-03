# Point Model

## Goal

Provide a strict, auditable Cobblestone Points economy where cobblestone conversion is the primary source.

## Core Rule

- `1 cobblestone = 1 Cobblestone Point`

## Conversion Rules

1. Conversion consumes physical cobblestone from player inventory.
2. Points grant is atomic and ledger-backed; inventory consume happens only after amount validation.
3. Partial consume is allowed when requested amount exceeds inventory, only if `allowPartial=true` in config.
4. Default behavior rejects when requested amount exceeds inventory.

## Ledger Rules

1. Ledger records every mutation with reason code.
2. Balance cannot go negative, including under concurrent purchases or refunds.
3. Operator adjustments require permission and are audit logged.
4. Seasonal rate overrides are independently logged with actor and timestamp.

## Reason Codes

- `COBBLE_CONVERT`
- `SHOP_PURCHASE`
- `ADMIN_ADJUST`
- `SEASONAL_OVERRIDE_APPLIED`
- `TEMPORARY_DIMENSION_REFUND`
