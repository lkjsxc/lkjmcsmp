# Home and Warp Command Contract

## Commands

- `/home [name]`
- `/sethome <name>`
- `/delhome <name>`
- `/homes`
- `/warp <name>`
- `/setwarp <name>`
- `/delwarp <name>`
- `/warps`

## Permissions

- `lkjmcsmp.home.use`
- `lkjmcsmp.home.manage`
- `lkjmcsmp.home.limit.<tier>`
- `lkjmcsmp.warp.use`
- `lkjmcsmp.warp.manage`

## Rules

1. Home names are case-insensitive and normalized.
2. Each player has a configurable home limit.
3. Warp creation and deletion are operator-restricted by default.
4. Home and warp teleports share teleport cooldown contract unless explicitly disabled.
5. `/homes` and `/warps` output sorted stable lists.
6. GUI lists must mirror command list ordering.

## Failures

- Duplicate name on create: rejected.
- Unknown name on delete/use: rejected with suggested nearest match where available.
- Limit exceeded: rejected with current and max counts.
