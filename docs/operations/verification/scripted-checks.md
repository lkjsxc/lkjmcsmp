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
- Profile command

## Minimum Assertions

- Command responds without server error.
- Expected success or failure message contains contract key phrase.
- Side effects are persisted when expected.
- `/tp` plain command path resolves to plugin behavior (or explicit diagnostic fallback).
- Teleport request creation emits requester and target notifications.
- Teleport stability delay enforces movement radius and emits cancellation when exceeded.
- RTP path does not emit thread-context failure errors in server logs.
- Initial trigger RTP completion state persists once per UUID.
- Hotbar slot `8` opens root menu on click/use.
- Hotbar menu item drop intent does not lose item and opens root menu.
- Hotbar slot `8` token cannot be moved by inventory manipulation vectors.
- Hotbar slot `8` token opens menu from cancelled/blocked interaction contexts.
- Hotbar slot `8` token does not appear in death drops.
- Hotbar token is resynchronized after respawn and item pickup.
- Non-token hotbar slot `5` interactions never open the menu.
- GUI `Back` controls render as arrow items.
- Decorative glass panes and info items do not emit unknown-action messages.
- GUI menus avoid background auto-refresh reopen loops.
- Picker menus render explicit manual `Refresh`.
- Homes deletion uses dedicated explicit deletion flow (not right-click semantic split).
- Homes GUI `Add Current Location` creates sequential names (`home-1`, `home-2`, ...).
- Shop item selection opens item detail purchase screen.
- Shop detail shows current points balance.
- Shop detail renders direct-buy quantity buttons (`1`, `2`, `4`, `8`, `16`, `32`, `64`).
- Shop cobblestone conversion button works regardless of held item.
- Pagination controls keep stable ordering across pages.
- Achievement GUI shows achievement status plus numeric progress text.
- Shop quantity purchase uses final item quantity (`1..64`) and computes deterministic totals; logs use `1 log = 16 Cobblestone Points`.
- `/tpaccept` opens requester picker when 2+ pending requests exist.
- Team disband action from GUI opens explicit confirm screen before execution.
- Team menu displays member names as player heads.
- GUI slot-map contract markers match shop-detail source layout (`19..25` direct-buy + `31` balance + `49` back).
- Action-bar source markers enforce deterministic state priority (teleport > combat > idle).
- Action-bar markers include teleport countdown/completion and combat 3-second HP-bar overlay.
- Action bar never stays blank after combat or teleport overlay expires; idle reclaims immediately.
- Action bar is re-sent continuously and does not rely on changed text to stay visible.
- Combat overlay text omits the literal `"HP"`; shows target name followed by HP bar only.
- Shop list renders service items with configured material and `"§dService Item — executes on purchase"` lore line.
- Temporary End Pass shop item is visible and purchasable.
- Temporary dimension creation respects the shop entry `environment` value (`THE_END`, `NETHER`, `NORMAL`).
- Temporary dimension purchase success is reported only after creator teleport succeeds; failures refund the exact deducted points.
- Temporary dimension closed rows survive until offline participant pending returns are consumed.
- Failed pending temporary-dimension transfers do not leave active participant rows.
- GUI action routing uses `lkjmcsmp:menu_action` metadata and does not branch on translated display labels.
- Language selector options are loaded from `lang/languages.yml`.
- Empty-hand right-click on stairs creates a seat and cleanup removes it on dismount/quit/death/teleport/block break.
- GUI menus render decorative borders with stained glass panes.
- `/profile` command opens the profile menu without error.

## Action Bar Assertions

1. Teleport countdown overlays include remaining seconds and expire cleanly on completion/cancel/failure.
2. Combat overlays include target name plus two-tone HP bar and expire after `3` seconds.
3. Idle HUD renders points and online count when no overlay is active.
4. No scoreboard probe path or sidebar dependency remains in runtime or smoke checks.
5. Action bar is never blank for an online player.

## Blocker Policy

- Any failed action-bar assertion is an acceptance blocker.
- Any failed GUI contract assertion is an acceptance blocker.
