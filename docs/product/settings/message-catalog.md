# Message Catalog

## Goal

Keep player-facing text in JSON files so LLM agents can add languages without touching gameplay code.

## Resource Layout

1. Language files live under `src/main/resources/lang/`.
2. File names are lower-case language codes, e.g. `en.json` and `ja.json`.
3. Each file is a flat JSON object from key to string.
4. English is the canonical fallback file.
5. Missing keys in a non-English file fall back to English.

## Key Rules

1. Keys use lower-case dotted names, e.g. `menu.root.title`.
2. Player-provided names are inserted as placeholders and are never translated.
3. Placeholders use `{name}` syntax.
4. Legacy `§` color codes are allowed for item names and lore.
5. Command literals, permission nodes, and config keys remain ASCII.

## Scope

The core localization surface includes:

1. Menu titles, button names, and lore.
2. Hotbar menu item display name and lore.
3. Core command and GUI chat feedback.
4. HUD/action-bar text emitted by plugin services.
5. Settings and language-change confirmations.

## Cross-References

- [language.md](language.md): language selection behavior
- [player-preferences.md](player-preferences.md): persisted language setting
