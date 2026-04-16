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
4. Milestone status transitions are monotonic unless explicitly reset by admin command.

## Retry Rules

1. Busy SQLite operations retry with bounded backoff.
2. Exhausted retries surface explicit command failure.
3. Failed writes are logged with operation key and actor.
