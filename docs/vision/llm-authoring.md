# LLM Authoring Rules

## Formatting Rules

- Use stable headings (`Goal`, `Rules`, `Contract`, `Verification`).
- Keep one requirement per bullet where possible.
- Use relative links only.
- Keep canonical definitions in one owner file.
- Delete stale requirements when behavior is removed.

## Topology Rules

- Every docs directory has one `README.md` table of contents.
- Every docs directory has multiple children.
- Parent `README.md` files must be updated in the same batch as child edits.

## Change Anatomy

1. Identify the owner doc for the behavior.
2. Update related command, operation, data, and architecture docs only where they add non-duplicate constraints.
3. Keep verification docs aligned with the exact scripted checks that will fail on regression.
4. Commit docs-only contract batches before dependent implementation batches.

## Length Rules

- Docs files: `<= 300` lines.
- Authored source files: `<= 200` lines.

## Terminology Rules

- Use `Cobblestone Points` for the player-facing currency generated from cobblestone conversion.
- Use `points` for internal ledger, code, and database field names.
- Use `party` for the lightweight team system.
- Use `achievement` for plugin-managed achievement state and contracts.

## GUI Visual Contracts

- Decorative border colors are canonical per menu category and must match [slot-maps.md](../product/gui/slot-maps.md).
- Border rendering uses `STAINED_GLASS_PANE` materials; never use solid blocks that could be confused with content.
- Info panels at slot `4` use `PAPER` with concise gray or gold names.
- Filler helper classes that contain only static layout data are exempt from the strict 180-line headroom target but must stay under 200 lines.
