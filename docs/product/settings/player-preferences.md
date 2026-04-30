# Player Preferences

## Storage

1. Preferences are stored in SQLite table `player_settings`.
2. Primary key is `player_uuid`.
3. `language` stores a lower-case language code.
4. `hotbar_menu_enabled` stores `1` for enabled and `0` for disabled.
5. `updated_at` stores mutation time as ISO-8601 text.

## Hotbar Menu Item

1. Enabled players receive the slot `8` menu token on join and respawn.
2. Disabled players never receive the token automatically.
3. Disabling removes every plugin menu token from inventory and cursor.
4. Enabling installs the token immediately.
5. The `/menu` command remains available regardless of this setting.

## Settings Menu Behavior

1. Root menu `Settings` opens the settings page.
2. `Hotbar Menu Item` toggles the persisted value immediately.
3. `Language` opens the language page.
4. Settings pages return to root with `Back`.
5. Toggling or choosing language refreshes the current menu so visible text reflects the new state.
