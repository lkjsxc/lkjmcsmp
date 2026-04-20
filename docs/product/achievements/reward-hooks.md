# Reward Hook Contract

## Reward Types

- Points grant
- Item grant
- Permission-limited cosmetic token

## Rules

1. Reward claim checks achievement completion status first.
2. Reward claim is atomic with claim-state update.
3. Duplicate claim attempts are rejected without side effects.
4. Inventory-full on item grant uses safe fallback:
   - drop at player location only if configured
   - otherwise reject and keep unclaimed status
5. Claim actions from GUI and `/achievement claim <key>` use the same service path.
6. Claim results include exact reason (`unknown achievement`, `not claimable`, or success).

## Audit Fields

- player UUID
- achievement key
- reward type
- claim timestamp
- success/failure outcome
