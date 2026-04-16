# Reward Hook Contract

## Reward Types

- Points grant
- Item grant
- Permission-limited cosmetic token

## Rules

1. Reward claim checks milestone completion status first.
2. Reward claim is atomic with claim-state update.
3. Duplicate claim attempts are rejected without side effects.
4. Inventory-full on item grant uses safe fallback:
   - drop at player location only if configured
   - otherwise reject and keep unclaimed status

## Audit Fields

- player UUID
- milestone key
- reward type
- claim timestamp
- success/failure outcome
