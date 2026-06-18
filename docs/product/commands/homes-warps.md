# Home and Warp Command Contract

## Commands

- `/home [name]`
- `/home create [name]`
- `/delhome <name>`
- `/homes`
- `/lkjmcsmp home list`
- `/lkjmcsmp home go [name]`
- `/lkjmcsmp home create [name]`
- `/lkjmcsmp home delete <name>`
- `/lkjmcsmp home buy-slot`
- `/lkjmcsmp warps list`
- `/lkjmcsmp warps go <name>`
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
2. Each player has a configurable base home limit plus purchased Home slot upgrades.
3. Purchased Home slot upgrades cap at 21 extra slots.
4. Warp creation and deletion are operator-restricted by default.
5. Home and warp teleports share teleport cooldown contract unless explicitly disabled.
6. `/homes` and `/warps` output sorted stable lists.
7. GUI lists must mirror command list ordering.
8. Homes GUI includes `Create Home`, equivalent to `/home create <generated-name>`.
9. `/home create [name]` and `/lkjmcsmp home create [name]` create or update the named home at the current location.
10. `create` with no name generates `home-<n>` using the first available positive sequential index (`home-1`, `home-2`, ...).
11. Homes GUI does not expose default-home shortcuts (`home create home` / `delhome home`) as dedicated GUI actions.
12. `Create Home` returns the chosen name in success feedback.
13. Direct Home command completion must work for both `/home ...` and the namespaced command literal `/lkjmcsmp:home ...`.
14. Home deletion is accessed through explicit deletion flow, not alternate click semantics.
15. Homes/warps GUI lists refresh on actions and avoid background auto-refresh reopen loops.
16. `/lkjmcsmp home` provides the same named-Home operations with explicit subcommands.
17. `/lkjmcsmp home buy-slot` buys the next Home slot upgrade from the Home surface.
18. Home slot upgrades start at `600` Points and are bought in order.
19. Buying a Home slot never goes through the Points Shop catalog.

## Failures

- Duplicate name on create: rejected.
- Unknown name on delete/use: rejected with suggested nearest match where available.
- Limit exceeded: rejected with current and max counts.
- GUI auto-name collision (stale client/state drift): server retries next sequential index before failing.
- Home slot already maxed: rejected before deduction.
- Insufficient points for Home slot: rejected before mutation.
