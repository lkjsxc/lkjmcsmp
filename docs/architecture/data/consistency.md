# Data Consistency Rules

## Transaction Rules

1. Points balance mutations use one SQLite transaction:
   - create missing player row at `0`
   - apply `balance = balance + delta`
   - reject and roll back negative balances
   - append ledger only after balance update succeeds
2. Cobblestone conversion consumes inventory only after requested amount is validated, then commits one positive points mutation.
3. Shop purchase debits points before inventory grant or service execution; service failures use compensating refunds. This does not apply to Home slot upgrades, which are entirely SQLite-backed.
4. Home slot upgrades commit the slot increment, Points debit, and purchase ledger entry in one transaction on one connection. They never use compensating refunds.

## Home Slot Purchase Boundary

1. The purchase command carries the player UUID and expected purchased-slot count. The fixed catalog, not caller-supplied prices, determines the next upgrade and its cost.
2. Acquire the SQLite writer through the slot insert/update before reading the balance; do not upgrade a stale read snapshot or retry a purchase automatically.
3. Compare the stored slot count with the expected count in the update. Concurrent requests for the same next slot permit at most one success; later explicit purchases may buy subsequent slots.
4. A stale/future request, insufficient balance, or exhausted catalog leaves all three tables unchanged, including timestamps and newly inserted zero rows.
5. Slot, debit, ledger, and commit failures roll back the whole operation. Propagate storage errors; do not report a failed refund as a successful recovery.
6. Only the transaction owner commits or rolls back. Connection-scoped mutation helpers are package-private and reject auto-commit connections; there is no public uncharged slot-grant path.
7. Success is returned only after commit. Build the success message from the committed result without another fallible database read.
8. Real SQLite tests cover exact-balance purchases, all 21 upgrades, stale requests, insufficient funds, write/commit failures, and concurrent requests under rollback-journal and WAL modes.
9. No schema migration or historical ledger rewrite is required. Existing balances, purchased slots, and old refund entries remain intact.

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
