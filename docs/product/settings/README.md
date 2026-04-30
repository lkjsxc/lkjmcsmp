# Settings

## Goal

Define player preference behavior for menus, language, and persistent UX choices.

## Rules

1. Settings are player-specific and persist by UUID.
2. Missing settings resolve to defaults without writing until the first mutation.
3. Settings are available through the root menu.
4. Setting changes apply immediately to the current online session.
5. Settings are not permission-gated unless a future setting explicitly declares a permission.
6. Messages shown during settings changes use the player's current language, except language-change confirmation may include both languages.

## Defaults

- Language: `en`
- Hotbar menu item: enabled

## Child Index

- [player-preferences.md](player-preferences.md): persistence and application rules
- [language.md](language.md): localization and language switching rules
- [message-catalog.md](message-catalog.md): JSON message file and key rules
