# Data Consistency Rules

## Transaction Rules

1. Points balance mutations use one SQLite transaction:
   - create missing player row at `0`
   - apply `balance = balance + delta`
   - reject and roll back negative balances
   - append ledger only after balance update succeeds
2. Cobblestone conversion consumes inventory only after requested amount is validated, then commits one positive points mutation.
3. Shop purchase debits points before inventory grant or service execution; service failures use compensating refunds.

## Integrity Rules

1. Points balance cannot drop below zero.
2. Party membership cardinality is max one party per player.
3. Home and warp names are normalized lowercase for keys.
4. Achievement status transitions are monotonic unless explicitly reset by admin command.
5. Temporary-dimension `RETURN_PENDING` participant rows outlive world deletion until pending returns succeed.
6. Temporary-dimension instance environment equals the actual created world environment.
7. `PENDING_TRANSFER` rows must not survive failed destination teleports.

## Retry Rules

1. SQLite busy scenarios are surfaced as explicit command failures.
2. Failed writes are logged with operation key and actor.
3. DAO callers handle `SQLException` as a failure path, not a retry loop.
