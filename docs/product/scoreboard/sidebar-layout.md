# Sidebar Layout Contract

## Goal

Provide a compact deterministic sidebar with stable visible output and stable internal identity.

## Title

- `lkjmcsmp SMP`

## Required Visible Lines

1. `Online: <count>` where `<count>` is current online players.
2. `Points: <balance>` where `<balance>` is player points (`0` on lookup failure).

## Required Ordering

1. `Online` line score is above `Points` line score.
2. Render pipeline must produce identical order for equal snapshots.

## Stable Entry Identity Rules

1. Internal scoreboard entries backing each visible line are stable fixed identifiers.
2. Entry identity must not depend on player name, locale, or random generation.
3. Render paths (join, periodic, retry, targeted) reuse the same entry identities.
4. Cleanup + rebuild preserves the same objective and entry identity contracts.

## Deterministic Formatting Rules

1. Visible text uses fixed labels (`Online`, `Points`) with integer values only.
2. Rendering does not depend on gradients, animation, or locale-specific number formatting.
3. Missing data uses explicit fallback values (`0` for points).
4. Equal snapshots must render equal title and equal visible line text.

## Visibility and Recovery Rules

1. Sidebar is enabled for all online players by default.
2. Managed objective identity is fixed and reused across join/reconcile/retry paths.
3. Every render reasserts `DisplaySlot.SIDEBAR` and reclaims ownership if overwritten.
4. Missing title or required lines is a recoverable failure and must trigger lifecycle retry/rebuild.
5. A blank or missing sidebar for an online player after retry window is a blocker regression.

## API Constraint

- Implementation must remain on Bukkit/Paper native scoreboard APIs only.
- External sidebar libraries and Packet/NMS sidebar fallback paths are not permitted.
