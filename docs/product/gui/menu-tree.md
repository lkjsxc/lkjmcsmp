# Menu Tree

## Inventory Size Contract

- All plugin menus use 54 slots (large chest layout).
- Shared navigation row reserves `Back` and context actions; manual `Refresh` is hidden by default.
- Open menus auto-refresh every `1` second by default and immediately after state-changing actions.
- `Back` control uses `ARROW` material across all menus.

## Root Menu (`/menu`)

1. Teleport
2. Homes
3. Warps
4. Team
5. Points Shop
6. Progression
7. Close Menu

## Teleport Menu

- Random Teleport
- TPA Request (target picker)
- TPA Here (target picker)
- Pending Request Accept (`/tpaccept` parity: direct accept on one pending, requester picker on multiple)
- Pending Request Deny
- Direct Player Teleport (permission-gated)

## Homes Menu

- Home list
- Left-click home entry: teleport
- Right-click home entry: delete
- Add Current Location (auto-name `home-<n>` using first available sequential index)
- Set default `home`
- Delete default `home`

## Warps Menu

- Warp list
- Teleport to warp
- Manage warps (permission-gated)

## Team Menu

- Team info
- Create team
- Invite player (target picker)
- Accept invite / leave / disband
- Team home and sethome

## Points Shop Menu

- Convert cobblestone
- Buy logs (quantity picker + explicit `Buy`; unit price `16` points per log)
- Buy dirt (quantity picker + explicit `Buy`)
- Exchange history
- Back (`ARROW`)

## Progression Menu

- Milestone list
- Progress ratio and percent per milestone
- Detailed milestone explanation text
- Reward preview
- Claim actions for `COMPLETED_UNCLAIMED`
- Back (`ARROW`)
