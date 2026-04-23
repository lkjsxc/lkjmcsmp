# Player Head Rendering Contract

## Goal

All `PLAYER_HEAD` items in plugin menus must display the correct player skin instead of the default Steve texture.

## Rules

1. Any menu item representing a specific player must set the skull owner via `SkullMeta`.
2. The skull owner is set by `OfflinePlayer` UUID where available.
3. If the player cannot be resolved, the head falls back to the generic Steve texture with the display name intact.
4. Generic decorative heads (e.g., root menu Team button with no specific player) may remain as default Steve.

## Affected Surfaces

- Player picker menus (`pick-tpa`, `pick-tpahere`, `pick-tp`, `pick-tpaccept`, `pick-invite`)
- Team menu member list
- Profile menu team info section (self head)

## Cross-References

- [../gui/team-layout.md](../gui/team-layout.md): team menu member display
- [../gui/slot-maps.md](../gui/slot-maps.md): picker menu layouts
