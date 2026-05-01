# Message Catalog

## Goal

Keep player-facing text in JSON files so LLM agents can add languages without touching gameplay code.

## Resource Layout

1. Language files live under `src/main/resources/lang/`.
2. `src/main/resources/lang/languages.yml` lists the default language and selectable languages.
3. File names are lower-case language codes, e.g. `en.json` and `ja.json`.
4. Each file is a flat JSON object from key to string.
5. English is the canonical fallback file.
6. Missing keys in a non-English file fall back to English.

## Key Rules

1. Keys use lower-case dotted names, e.g. `menu.root.title`.
2. Player-provided names are inserted as placeholders and are never translated.
3. Placeholders use `{name}` syntax.
4. Legacy `§` color codes are allowed for item names and lore.
5. Command literals, permission nodes, and config keys remain ASCII.
6. GUI action routing never depends on translated text.

## Scope

The core localization surface includes:

1. Menu titles, button names, and lore.
2. Hotbar menu item display name and lore.
3. Core command and GUI chat feedback.
4. HUD/action-bar text emitted by plugin services.
5. Settings and language-change confirmations.

## Language Selection

1. The settings menu renders all languages from `languages.yml`.
2. Adding a language requires a catalog JSON file and registry entry, not Java code changes.
3. Unsupported or missing player language settings fall back to the configured default.

## Cross-References

- [language.md](language.md): language selection behavior
- [player-preferences.md](player-preferences.md): persisted language setting
