# Sidebar Layout Contract

## Goal

Provide a compact SMP sidebar that stays visible and deterministic for all online players.

## Title

- `lkjmcsmp SMP`

## Required Lines

1. `Online: <count>` where `<count>` is current online players.
2. `Points: <balance>` where `<balance>` is player points (`0` on lookup failure).

## Deterministic Formatting Rules

1. Line ordering is stable.
2. Line text uses fixed labels (`Online`, `Points`) and integer values only.
3. Rendering does not depend on colors, gradients, animation, or locale-specific formatting.
4. Missing data uses explicit fallback values (`0` for points).
5. Equal snapshots must render equal title and line text.

## Visibility and Recovery Rules

1. Sidebar is enabled for all online players by default.
2. Managed objective identity is fixed and reused across join/reconcile/retry paths.
3. Every render reasserts `DisplaySlot.SIDEBAR` and reclaims ownership if overwritten.
4. Missing title or required lines is a recoverable failure and must trigger lifecycle retry/rebuild.
5. A blank or missing sidebar for an online player after retry window is a blocker regression.

## API Constraint

- Implementation must remain on Bukkit/Paper native scoreboard APIs only; no external sidebar library fallback is permitted.
