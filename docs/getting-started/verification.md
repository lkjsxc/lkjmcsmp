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
4. Teleport, menu lock, and action-bar regressions are treated as acceptance blockers.
5. HUD regressions include missing teleport countdown/completion feedback, missing combat overlay behavior, and action-bar blank gaps.

## Minimal Smoke Assertions

- Plugin appears in server plugin list.
- Core command registrations exist (`tp`, `rtp`, `tpa`, `home`, `team`, `warp`, `achievement`, `ach`, `tempdim`).
- GUI root menu can be opened by command.
- Cobblestone conversion command path succeeds for an operator test account.
- Hotbar slot `8` menu entrypoint opens GUI and cannot be dropped.
- Hotbar slot `8` menu entrypoint is visible after respawn and pickup synchronization.
- Hotbar slot `8` menu entrypoint opens from cancelled/blocked interaction contexts.
- Non-token slot interactions do not open the menu.
- GUI menus refresh state on actions without background auto-refresh reopen loops.
- Picker menus expose explicit manual refresh control.
- Homes GUI add-current-location path yields sequential auto names (`home-1`, `home-2`, ...).
- Homes deletion uses explicit dedicated deletion flow.
- Team disband GUI flow requires explicit confirm screen.
- Shop detail exposes points balance indicator.
- Shop detail direct-buy path uses final item quantity buttons (`1`, `2`, `4`, `8`, `16`, `32`, `64`) and honors per-item rate math (`1` log = `16` points).
- Initial trigger-zone RTP shows a countdown before first-entry random teleport.
- Shop detail slot-map markers match canonical layout (`19..25`, `31`, `49`).
- `/tpaccept` with multiple pending requests opens requester picker GUI.
- Action-bar markers enforce always-on idle HUD (`points+online`), teleport countdown/completion states, and 3-second combat target HP bar overlay.
- Stair sitting works with empty-hand right-click and cleans up seat entities.
