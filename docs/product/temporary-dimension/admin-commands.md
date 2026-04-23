# Temporary Dimension Admin Commands

## Summary

Operators can inspect and forcibly close active temporary dimension instances.

## Commands

- `/tempdim list` — list active instances.
- `/tempdim info <id>` — show instance details (creator, remaining time, state, participant count, world, environment).
- `/tempdim forceclose <id>` — immediately expire and clean up an instance.

## Permissions

- `lkjmcsmp.temporarydimension.use` — default: `true`
- `lkjmcsmp.temporarydimension.admin` — default: `op`

## Logging

1. `creation requested` — player triggers purchase.
2. `purchase succeeded/failed` — result of points transaction.
3. `instance created` — world creation complete.
4. `players transferred` — count and target world.
5. `expiry started` — instance transitions to `EXPIRING`.
6. `cleanup completed` — world folder deleted and records removed.
7. `recovery/orphan cleanup on startup` — reconciled missing worlds.

## Cross-References

- [lifecycle.md](lifecycle.md): states and expiry sequence
- [purchase-and-creation.md](purchase-and-creation.md): player-facing purchase flow
