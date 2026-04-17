# Verification Entry

## Canonical Compose Bundle

```bash
docker compose -f docker-compose.yml -f docker-compose.verify.yml build verify smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm verify
docker compose -f docker-compose.yml -f docker-compose.verify.yml up -d folia
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml down -v
```

## Required Behavior

1. `verify` returns zero only when build/tests/docs/line checks all pass.
2. `smoke` returns zero only when plugin startup and scripted command checks pass.
3. Non-zero from any step blocks acceptance.
4. Teleport, menu lock, and scoreboard regressions are treated as acceptance blockers.
5. Scoreboard regressions include missing sidebar visibility, non-deterministic line output, failed recovery, or illegal external sidebar dependency usage.

## Minimal Smoke Assertions

- Plugin appears in server plugin list.
- Core command registrations exist (`tp`, `rtp`, `tpa`, `home`, `team`, `warp`).
- GUI root menu can be opened by command.
- Cobblestone conversion command path succeeds for an operator test account.
- Hotbar slot `8` menu entrypoint opens GUI and cannot be dropped.
- GUI menus auto-refresh state without a manual refresh control.
- Homes GUI add-current-location path yields sequential auto names (`home-1`, `home-2`, ...).
- Shop quantity path honors unit rate (`1` log = `16` points) in total-cost math.
- `/tpaccept` with multiple pending requests opens requester picker GUI.
- Scoreboard sidebar appears with online-count and points lines.
- Scoreboard join render and periodic reconcile render produce the same title and line ordering.
- Scoreboard recovers visibility after objective removal within documented retry/reconcile window.
- Scoreboard dependency set must remain Bukkit/Paper-only (no external sidebar library).

## Scoreboard Blocker Rule

- Treat any scoreboard smoke failure as merge-blocking, even when other smoke checks pass.

## Assumptions

- Verification environment can execute objective-removal injection and inspect logs for scoreboard retry evidence.
