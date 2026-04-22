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

1. `4`: Info Panel (`PAPER`)
2. `19`: Teleport
3. `20`: Homes
4. `21`: Warps
5. `22`: Team
6. `23`: Points Shop
7. `24`: Achievement
8. `25`: Temporary End
9. `50`: Close Menu

## Teleport Menu (`lkjmcsmp :: teleport`)

1. `4`: Info Panel (`PAPER`)
2. `10`: Random Teleport
3. `11`: Request Teleport
4. `12`: Request Here
5. `13`: Accept Request / No Pending Requests
6. `14`: Deny Request / No Pending Requests
7. `15`: Direct Teleport / Direct Teleport (Locked)
8. `49`: Back

## Homes Menu (`lkjmcsmp :: homes`)

1. `4`: Info Panel (`PAPER`)
2. `0..44`: paged home entries (skipping border slots)
3. `45`: Add Current Location
4. `46`: Page Prev
5. `47`: Page Next
6. `48`: Page Info
7. `49`: Back
8. `50`: Open Home Deletion Page

## Homes Delete Menu (`lkjmcsmp :: homes-delete`)

1. `4`: Info Panel (`PAPER`)
2. `0..44`: paged deletion entries (skipping border slots)
3. `45`: Cancel Deletion (return to homes list)
4. `46`: Page Prev
5. `47`: Page Next
6. `48`: Page Info
7. `49`: Back

## Warps Menu (`lkjmcsmp :: warps`)

1. `4`: Info Panel (`PAPER`)
2. `0..44`: paged warp entries (skipping border slots)
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back

## Team Menu (`lkjmcsmp :: team`)

1. `4`: Info Panel (`PAPER`)
2. `10`: Team Info
3. `19`: Create Team
4. `20`: Invite Player
5. `21`: Accept Invite
6. `23`: Team Home
7. `24`: Set Team Home
8. `25`: Leave Team
9. `31`: Disband Team
10. `49`: Back

## Team Disband Confirm Menu (`lkjmcsmp :: team-disband-confirm`)

1. `4`: Info Panel (`PAPER`)
2. `22`: Confirmation Summary
3. `30`: Confirm Disband
4. `32`: Cancel
5. `49`: Back

## Shop List Menu (`lkjmcsmp :: shop`)

1. `4`: Info Panel — Current Points (`PAPER`)
2. `0..44`: paged shop entries (skipping border slots)
3. `45`: Convert Cobblestone
4. `46`: Page Prev
5. `47`: Page Next
6. `48`: Page Info
7. `49`: Back

## Shop Detail Menu (`lkjmcsmp :: shop-detail`)

1. `4`: Info Panel — Selected Item Summary (`PAPER`)
2. `13`: Selected Item Summary
3. `31`: Points Balance
4. `20`: Buy x1
5. `21`: Buy x2
6. `22`: Buy x4
7. `23`: Buy x8
8. `24`: Buy x16
9. `25`: Buy x32
10. `26`: Buy x64
11. `49`: Back

## Achievement Menu (`lkjmcsmp :: achievement`)

1. `4`: Info Panel (`PAPER`)
2. `0..44`: paged achievement entries (skipping border slots)
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back

## Temporary End Menu (`lkjmcsmp :: temporary-end`)

1. `4`: Info Panel (`PAPER`)
2. `13`: Description Summary
3. `22`: Purchase
4. `31`: Points Balance
5. `49`: Back

## Picker Menus (`pick-*`)

1. `4`: Info Panel (`PAPER`)
2. `0..44`: paged player/requester entries (skipping border slots)
3. `46`: Page Prev
4. `47`: Page Next
5. `48`: Page Info
6. `49`: Back
7. `50`: Refresh

## Consistency Rules

1. Back is always rendered at slot `49` with `ARROW`.
2. Picker `Refresh` appears only on picker menus.
3. Pagination controls are rendered only for paged menus and keep shared slot identities.
4. Non-paged menus may leave shared pagination slots empty.
5. Contract changes to slot maps must be reflected in both docs and implementation in the same batch.
6. Decorative borders take precedence over empty slots but never overwrite functional items.
