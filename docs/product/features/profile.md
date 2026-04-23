# Profile Contract

## Goal

Provide a self-profile command and menu so players can view their own stats without relying on separate commands.

## Command

- `/profile`
- Permission: `lkjmcsmp.profile.use` (default: `true`)

## Profile Menu (`lkjmcsmp :: profile`)

- Slot `4`: Info Panel (`PAPER`) showing player name.
- Slot `10`: Points balance (`SUNFLOWER`). Shows current Maruishi Points.
- Slot `12`: Team info (`PLAYER_HEAD` with own skull). Shows party name and role. Click opens Team menu.
- Slot `14`: Achievement summary (`BOOK`). Shows completed count / total count and in-progress count. Click opens Achievement menu.
- Slot `16`: Playtime (`CLOCK`). Shows session playtime (or total if tracked).
- Slot `49`: Back to root menu (`ARROW`).
- Border: `LIME_STAINED_GLASS_PANE`.

## Rules

1. All values are read-only; the profile menu is informational.
2. Clicking a section button opens the corresponding dedicated menu.
3. The menu refreshes each time it is opened.

## Cross-References

- [../gui/menu-tree.md](../gui/menu-tree.md): root menu placement
- [../gui/slot-maps.md](../gui/slot-maps.md): canonical slot positions
