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
- Pseudo-advancement claim flow
- GUI root menu open
- Scoreboard sidebar lifecycle and recovery

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
- Shop detail quantity resets to baseline on each detail-open.
- Pagination controls keep stable ordering across pages.
- Progression GUI shows milestone status plus numeric progress text.
- Shop quantity purchase uses final item quantity (`1..64`) and computes deterministic totals; logs use `1 log = 16 points`.
- `/tpaccept` opens requester picker when 2+ pending requests exist.
- GUI slot-map contract markers match shop-detail source layout (`20..26` control cluster + `31` buy + `49` back).
- Scoreboard renderer source markers enforce canonical objective/entry/slot ownership model.
- Scoreboard implementation dependency set excludes external sidebar libraries.
- Runtime scoreboard probe command (`/lkjverify scoreboard`) succeeds (full simulation or explicit unsupported-path detection).

## Scoreboard Reliability Assertions

1. Runtime probe attempts objective-overwrite injection and rebuild reclaim assertions.
2. If Folia runtime rejects probe mutations with `UnsupportedOperationException`, probe records explicit unsupported-path detection and still returns success.
3. Renderer/source contract markers include fixed objective identity, stable entries, and sidebar ownership.
4. No external sidebar library classes or artifacts exist in plugin dependency graph.

## Blocker Policy

- Any failed scoreboard assertion is an acceptance blocker.
- Missing runtime scoreboard probe evidence is an acceptance blocker.
- Any failed runtime scoreboard probe assertion is an acceptance blocker.

## Assumptions

- Smoke harness has operator-level permission to execute `/lkjverify scoreboard` from RCON console.
