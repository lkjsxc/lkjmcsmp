# Data Consistency Rules

## Transaction Rules

1. Cobblestone conversion is one transaction:
   - inventory consume
   - points balance increment
   - ledger append
2. Shop purchase is one transaction:
   - points decrement
   - item grant confirmation
   - ledger append

## Integrity Rules

1. Points balance cannot drop below zero.
2. Party membership cardinality is max one party per player.
3. Home and warp names are normalized lowercase for keys.
4. Achievement status transitions are monotonic unless explicitly reset by admin command.

## Retry Rules

1. SQLite busy scenarios are surfaced as explicit command failures.
2. Failed writes are logged with operation key and actor.
3. DAO callers handle `SQLException` as a failure path, not a retry loop.
