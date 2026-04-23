# LLM Authoring Rules

## Formatting Rules

- Use stable headings (`Goal`, `Rules`, `Contract`, `Verification`).
- Keep one requirement per bullet where possible.
- Use relative links only.
- Keep canonical definitions in one owner file.

## Topology Rules

- Every docs directory has one `README.md` table of contents.
- Every docs directory has multiple children.
- Parent `README.md` files must be updated in the same batch as child edits.

## Length Rules

- Docs files: `<= 300` lines.
- Authored source files: `<= 200` lines.

## Terminology Rules

- Use `Maruishi Points` for the player-facing currency generated from cobblestone conversion.
- Use `points` for internal ledger, code, and database field names.
- Use `party` for the lightweight team system.
- Use `achievement` for plugin-managed achievement state and contracts.

## GUI Visual Contracts

- Decorative border colors are canonical per menu category and must match [slot-maps.md](../product/gui/slot-maps.md).
- Border rendering uses `STAINED_GLASS_PANE` materials; never use solid blocks that could be confused with content.
- Info panels at slot `4` use `PAPER` with concise gray or gold names.
- Filler helper classes that contain only static layout data are exempt from the strict 180-line headroom target but must stay under 200 lines.
