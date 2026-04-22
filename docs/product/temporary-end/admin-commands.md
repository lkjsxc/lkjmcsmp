# Temporary End Admin Commands

## Command

- `/tempend list` — list active instances.
- `/tempend info <id>` — show instance details (creator, remaining time, state, participant count, world).
- `/tempend forceclose <id>` — immediately expire and clean up an instance.

## Permissions

- `lkjmcsmp.temporaryend.admin` — default: `op`

## Logging

1. `creation requested` — player triggers purchase.
2. `purchase succeeded/failed` — result of points transaction.
3. `instance created` — world creation complete.
4. `players transferred` — count and target world.
5. `expiry started` — instance transitions to `EXPIRING`.
6. `cleanup completed` — world folder deleted and records removed.
7. `recovery/orphan cleanup on startup` — reconciled missing worlds.
