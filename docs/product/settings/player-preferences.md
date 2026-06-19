# Player Preferences

## Storage

1. Preferences are stored in SQLite table `player_settings`.
2. Primary key is `player_uuid`.
3. `language` stores a lower-case language code.
4. `hotbar_menu_enabled` stores `1` for enabled and `0` for disabled.
5. `action_bar_enabled` stores `1` for enabled and `0` for disabled.
6. `tips_enabled` stores `1` for enabled and `0` for disabled.
7. `updated_at` stores mutation time as ISO-8601 text.

## Hotbar Menu Item

1. Enabled players receive the slot `8` menu token on join and respawn.
2. Disabled players never receive the token automatically.
3. Disabling immediately removes every plugin menu token from slot `8`, the rest of the inventory, and cursor.
4. Enabling installs the token immediately.
5. The `/menu` command remains available regardless of this setting.

## Settings Menu Behavior

1. Root menu `Settings` opens the settings page.
2. `Action Bar HUD` toggles all plugin action-bar output immediately.
3. `Hotbar Menu Item` toggles the persisted value immediately.
4. `Tips` toggles periodic chat tips immediately.
5. `Language` opens the language page.
6. Settings pages return to root with `Back`.
7. Toggling or choosing language refreshes the current menu so visible text reflects the new state.

## Action Bar HUD

1. Enabled players receive the idle HUD and temporary action-bar overlays.
2. Disabled players receive no action-bar messages from this plugin.
3. Disabling does not send a blank action bar; other plugins may continue using the channel.
4. Chat messages and menus remain available while the action bar HUD is disabled.

## Tips

1. Enabled players receive scheduled command-discovery tips.
2. Disabled players are skipped during each scheduled tip pass.
3. The setting only controls per-player delivery; global scheduling remains controlled by `tips.enabled`.
