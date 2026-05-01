# Language

## Supported Languages

1. `src/main/resources/lang/languages.yml` defines selectable languages.
2. `en`: English, required default baseline.
3. `ja`: Japanese, required baseline translation.

## Selection Rules

1. Players switch language from the Settings menu.
2. The chosen language applies to menu labels, menu lore, and core chat messages implemented by the plugin.
3. Missing translation keys fall back to English.
4. Unsupported stored language codes fall back to the configured default and are corrected on next mutation.
5. Language choice persists across sessions.
6. New players default to the configured default language until they choose another language.
7. Adding a new language requires only a registry entry and JSON catalog.

## Japanese UX Rules

1. Japanese text should be concise and action-oriented.
2. Command literals such as `/menu`, `/tpaccept`, and permission nodes remain ASCII.
3. Names supplied by players are never translated.
4. Numeric values and item quantities remain Arabic numerals.

## Cross-References

- [message-catalog.md](message-catalog.md): JSON message format
