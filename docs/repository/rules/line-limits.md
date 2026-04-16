# Line Limits Contract

## Hard Limits

- Docs files: `<= 300` lines
- Authored source files: `<= 200` lines

## Headroom Targets

- Prefer source files below `180` lines where cohesive splitting is possible.
- Files above `190` lines are split candidates when edited.

## Enforcement

- `scripts/check_lines.py` is the mandatory gate.
- New exclusions require explicit documentation update in this file.
