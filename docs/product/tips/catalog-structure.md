# Tip Catalog Structure

## Goal

Keep a large multilingual tip set maintainable by category and language.

## Resource Layout

1. Tip resources live under `src/main/resources/tips/`.
2. One directory exists per language code, e.g. `tips/en/` and `tips/ja/`.
3. Each language directory contains category YAML files.
4. Category files use lower-case names such as `home.yml` or `teleport.yml`.
5. Each file has one top-level key: `tips`.
6. `tips` is an ordered list of one-line strings.
7. The bundled category set is shared across languages.
8. English is the canonical fallback catalog.

## Bundled Categories

1. `home`
2. `navigation`
3. `shop`
4. `teleport`
5. `team`
6. `settings`
7. `achievements`
8. `safety`
9. `temporary-dimension`
10. `profile`

## Validation Expectations

1. English and Japanese catalogs should have the same category files.
2. English and Japanese catalogs should have the same number of tips.
3. The bundled catalog target is 20 tips per category and 200 tips per language.
