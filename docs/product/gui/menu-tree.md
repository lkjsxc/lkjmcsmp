# Menu Tree

## Inventory Size Contract

- All plugin menus use 54 slots (large chest layout).
- Shared navigation row reserves context action, paging controls, and `Back`; picker menus may also expose manual `Refresh`.
- Menus refresh immediately after state changes.
- Background auto-refresh reopen loops are disallowed.
- `Back` control uses `ARROW` material across all menus.
- Slot positions are canonical in [slot-maps.md](slot-maps.md).
- Decorative borders frame every menu using the canonical color per category.

## Root Menu (`/menu`)

1. Teleport
2. Homes
3. Warps
4. Team
5. Points Shop
6. Achievement
7. Profile
8. Close Menu

## Temporary Dimension Access

- The dimension pass is not a root-menu entry.
- Players reach it through **Points Shop -> Mysterious Egg -> Purchase**.

## Teleport Menu

- Random Teleport
- TPA Request (target picker)
- TPA Here (target picker)
- Pending Request Accept (`/tpaccept` parity: direct accept on one pending, requester picker on multiple)
- Pending Request Deny
- Direct Player Teleport (permission-gated)

## Homes Menu

- Home list (paginated)
- Home entry click: teleport
- Add Current Location (auto-name `home-<n>` using first available sequential index)
- Open Home Deletion Page

## Homes Deletion Page

- Home list (paginated)
- Home entry click: delete with explicit deletion affordance
- Back (`ARROW`)

## Warps Menu

- Warp list (paginated)
- Teleport to warp
- Back (`ARROW`)

## Team Menu

- Team info
- Member list (player heads with names)
- Create team
- Invite player (target picker)
- Accept invite / leave / disband
- Disband opens confirm screen before execution
- Team home and sethome
- Canonical slot mapping is defined in `team-layout.md` and `slot-maps.md`

## Team Disband Confirm Menu

- Warning summary
- Confirm disband
- Cancel/back
- Returns to Team menu after completion or cancel

## Points Shop Menu

- Item list (paginated)
- Convert cobblestone
- Select item to open purchase detail page
- Back (`ARROW`)

## Points Shop Detail Menu

- Selected item description and per-item pricing
- Current points balance indicator
- Direct-buy quantity buttons (`1`, `2`, `4`, `8`, `16`, `32`, `64`) for physical items
- Service items (e.g. temporary dimension pass) show a single Purchase button instead of quantity buttons
- No separate quantity-setting or buy-confirm button
- Purchase executes only when balance is sufficient
- Detail view stays open after physical-item purchase and refreshes affordability state
- Back (`ARROW`)

## Achievement Menu

- Achievement list (paginated)
- Progress ratio and percent per achievement
- Detailed achievement explanation text
- Reward preview
- Claim actions for `COMPLETED_UNCLAIMED`
- Back (`ARROW`)

## Profile Menu

- Points balance
- Team info and role
- Achievement summary
- Playtime
- Back (`ARROW`)

## Picker Menus (`pick-*`)

- `0..44`: paged player/requester entries
- `46`: Page Prev
- `47`: Page Next
- `48`: Page Info
- `49`: Back
- `50`: Refresh

## Cross-References

- [slot-maps.md](slot-maps.md): canonical slot positions
- [team-layout.md](team-layout.md): team-specific rules
- [shop-actions.md](shop-actions.md): purchase and conversion behavior
