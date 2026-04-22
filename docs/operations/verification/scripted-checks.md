# Scripted Check Contract

## Scope

Extended smoke suite:

1. Happy-path check per major system.
2. Permission-denial check per major system.
3. Cooldown/error-path check per major system where applicable.

## Major Systems

- Teleport (`tp`, `tpa`, `rtp`)
- Homes and warps
- Party operations
- Points conversion and shop purchase
- Achievement claim flow
- GUI root menu open
- Action bar HUD lifecycle and overlays
- Temporary End purchase and creation

## Minimum Assertions

- Command responds without server error.
- Expected success or failure message contains contract key phrase.
- Side effects are persisted when expected.
- `/tp` plain command path resolves to plugin behavior (or explicit diagnostic fallback).
- Teleport request creation emits requester and target notifications.
- Teleport stability delay enforces movement radius and emits cancellation when exceeded.
- RTP path does not emit thread-context failure errors in server logs.
- First-join RTP marker state persists once per UUID.
- Hotbar slot `8` opens root menu on click/use.
- Hotbar menu item drop intent does not lose item and opens root menu.
- Hotbar slot `8` token cannot be moved by inventory manipulation vectors.
- Hotbar slot `8` token opens menu from cancelled/blocked interaction contexts.
- GUI `Back` controls render as arrow items.
- GUI menus avoid background auto-refresh reopen loops.
- Picker menus render explicit manual `Refresh`.
- Homes deletion uses dedicated explicit deletion flow (not right-click semantic split).
- Homes GUI `Add Current Location` creates sequential names (`home-1`, `home-2`, ...).
- Shop item selection opens item detail purchase screen.
- Shop detail shows current points balance.
- Shop detail renders direct-buy quantity buttons (`1`, `2`, `4`, `8`, `16`, `32`, `64`).
- Pagination controls keep stable ordering across pages.
- Achievement GUI shows achievement status plus numeric progress text.
- Shop quantity purchase uses final item quantity (`1..64`) and computes deterministic totals; logs use `1 log = 16 points`.
- `/tpaccept` opens requester picker when 2+ pending requests exist.
- Team disband action from GUI opens explicit confirm screen before execution.
- GUI slot-map contract markers match shop-detail source layout (`20..26` direct-buy + `31` balance + `49` back).
- Action-bar source markers enforce deterministic state priority (teleport > combat > idle).
- Action-bar markers include teleport countdown/completion and combat 3-second HP-bar overlay.
- Action bar never stays blank after combat or teleport overlay expires; idle reclaims immediately.
- Combat overlay text omits the literal `"HP"`; shows target name followed by HP bar only.
- Root menu includes `Temporary End` entry.
- Temporary End shop item is visible and purchasable.
- GUI menus render decorative borders with stained glass panes.

## Action Bar Assertions

1. Teleport countdown overlays include remaining seconds and expire cleanly on completion/cancel/failure.
2. Combat overlays include target name plus two-tone HP bar and expire after `3` seconds.
3. Idle HUD renders points and online count when no overlay is active.
4. No scoreboard probe path or sidebar dependency remains in runtime or smoke checks.
5. Action bar is never blank for an online player.

## Blocker Policy

- Any failed action-bar assertion is an acceptance blocker.
- Any failed GUI contract assertion is an acceptance blocker.
