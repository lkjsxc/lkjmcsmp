# Home and Warp Command Contract

## Commands

- `/home [name]`
- `/sethome [name]`
- `/delhome <name>`
- `/homes`
- `/homes addcurrent [name]`
- `/warp <name>`
- `/setwarp <name>`
- `/delwarp <name>`
- `/warps`

## Permissions

- `lkjmcsmp.home.use`
- `lkjmcsmp.home.manage`
- `lkjmcsmp.warp.use`
- `lkjmcsmp.warp.manage`

## Rules

1. Home names are case-insensitive and normalized.
2. Each player has a configurable home limit.
3. Warp creation and deletion are operator-restricted by default.
4. Home and warp teleports share teleport cooldown contract unless explicitly disabled.
5. `/homes` and `/warps` output sorted stable lists.
6. GUI lists must mirror command list ordering.
7. Homes GUI includes `Add Current Location`, equivalent to `/sethome <generated-name>`.
8. `/sethome [name]` and `/homes addcurrent [name]` create or update the named home at the current location.
9. `/homes addcurrent` with no name generates `home-<n>` using the first available positive sequential index (`home-1`, `home-2`, ...).
10. `/sethome` with no name uses `home`.
11. `Add Current Location` returns the chosen name in success feedback.
12. Homes GUI does not expose default-home shortcuts (`sethome home` / `delhome home`) as dedicated GUI actions.
13. Home deletion is accessed through explicit deletion flow, not alternate click semantics.
14. Homes/warps GUI lists refresh on actions and avoid background auto-refresh reopen loops.

## Failures

- Duplicate name on create: rejected.
- Unknown name on delete/use: rejected with suggested nearest match where available.
- Limit exceeded: rejected with current and max counts.
- GUI auto-name collision (stale client/state drift): server retries next sequential index before failing.
