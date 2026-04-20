# GUI Slot Map Contract

## Goal

Define canonical item positions for every plugin menu so layout remains predictable and machine-auditable.

## Shared Navigation Row

For 54-slot menus, bottom-row control slots are:

1. `45`: context action (menu-specific)
2. `46`: page previous
3. `47`: page next
4. `48`: page info
5. `49`: back (`ARROW`)
6. `50`: close/refresh (menu-specific)

## Root Menu (`lkjmcsmp :: menu`)

1. `19`: Teleport
2. `20`: Homes
3. `21`: Warps
4. `22`: Team
5. `23`: Points Shop
6. `24`: Achievement
7. `50`: Close Menu

## Teleport Menu (`lkjmcsmp :: teleport`)

1. `10`: Random Teleport
2. `11`: Request Teleport
3. `12`: Request Here
4. `13`: Accept Request / No Pending Requests
5. `14`: Deny Request / No Pending Requests
6. `15`: Direct Teleport / Direct Teleport (Locked)
7. `49`: Back

## Homes Menu (`lkjmcsmp :: homes`)

1. `0..44`: paged home entries
2. `45`: Add Current Location
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back
7. `50`: Open Home Deletion Page

## Homes Delete Menu (`lkjmcsmp :: homes-delete`)

1. `0..44`: paged deletion entries
2. `45`: Cancel Deletion (return to homes list)
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back

## Warps Menu (`lkjmcsmp :: warps`)

1. `0..44`: paged warp entries
2. `46`: Page Prev
3. `47`: Page Next
4. `48`: Page Info
5. `49`: Back

## Team Menu (`lkjmcsmp :: team`)

1. `10`: Team Info
2. `19`: Create Team
3. `20`: Invite Player
4. `21`: Accept Invite
5. `23`: Team Home
6. `24`: Set Team Home
7. `25`: Leave Team
8. `31`: Disband Team
9. `49`: Back

## Team Disband Confirm Menu (`lkjmcsmp :: team-disband-confirm`)

1. `22`: Confirmation Summary
2. `30`: Confirm Disband
3. `32`: Cancel
4. `49`: Back

## Shop List Menu (`lkjmcsmp :: shop`)

1. `0..44`: paged shop entries
2. `45`: Convert Cobblestone
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back

## Shop Detail Menu (`lkjmcsmp :: shop-detail`)

1. `13`: Selected Item Summary
2. `31`: Points Balance
3. `20`: Buy x1
4. `21`: Buy x2
5. `22`: Buy x4
6. `23`: Buy x8
7. `24`: Buy x16
8. `25`: Buy x32
9. `26`: Buy x64
10. `49`: Back

## Achievement Menu (`lkjmcsmp :: achievement`)

1. `0..44`: paged achievement entries
2. `46`: Page Prev
3. `47`: Page Next
4. `48`: Page Info
5. `49`: Back

## Picker Menus (`pick-*`)

1. `0..44`: paged player/requester entries
2. `46`: Page Prev
3. `47`: Page Next
4. `48`: Page Info
5. `49`: Back
6. `50`: Refresh

## Consistency Rules

1. Back is always rendered at slot `49` with `ARROW`.
2. Picker `Refresh` appears only on picker menus.
3. Pagination controls are rendered only for paged menus and keep shared slot identities.
4. Non-paged menus may leave shared pagination slots empty.
5. Contract changes to slot maps must be reflected in both docs and implementation in the same batch.
