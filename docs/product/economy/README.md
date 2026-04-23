# Economy Contracts

## Goal

Define how Cobblestone Points are generated, stored, and exchanged for items.

## Rules

1. Cobblestone Points behavior must remain ledger-backed and auditable.
2. Conversion and purchase paths must share canonical rate definitions.
3. Error outcomes must explain missing Cobblestone Points or missing resources explicitly.
4. Shop purchases use list-to-detail navigation with explicit final-item quantity selection (`1..64`).
5. Shop quantity selection is not a multiplier model; selected quantity is the delivered item count.

## Child Index

- [point-model.md](point-model.md): Cobblestone Points generation and ledger invariants
- [exchange-rates.md](exchange-rates.md): shop rate table and override policy
