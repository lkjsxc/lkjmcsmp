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
- GUI menus do not render manual `Refresh`; state-changing actions update visible state without manual reopen.
- Homes deletion uses dedicated explicit deletion flow (not right-click semantic split).
- Homes GUI `Add Current Location` creates sequential names (`home-1`, `home-2`, ...).
- Shop item selection opens item detail purchase screen.
- Shop detail quantity resets to baseline on each detail-open.
- Pagination controls keep stable ordering across pages.
- Progression GUI shows milestone status plus numeric progress text.
- Shop quantity purchase computes deterministic totals; logs use `1 log = 16 points`.
- `/tpaccept` opens requester picker when 2+ pending requests exist.
- Scoreboard sidebar renders for online player with `online` and `points` lines.
- Scoreboard implementation dependency set excludes external sidebar libraries.

## Scoreboard Reliability Assertions

1. Sidebar appears for an online operator test player with canonical title and required lines.
2. Join path and periodic reconcile path produce identical line order and labels.
3. Injected sidebar-objective removal is recovered within bounded retry/reconcile window.
4. Recovery attempts emit structured logs with `trigger`, `playerUuid`, and `attempt`.
5. No external sidebar library classes or artifacts exist in plugin dependency graph.
6. Reconcile reclaims `DisplaySlot.SIDEBAR` when overwritten by another objective.

## Blocker Policy

- Any failed scoreboard assertion is an acceptance blocker.
- Missing sidebar recovery evidence is an acceptance blocker.

## Assumptions

- Smoke harness has operator-level permission to run vanilla `/scoreboard` commands for failure injection.
- Smoke harness can read server logs to assert retry/recovery logging behavior.
