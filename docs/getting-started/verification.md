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

## Minimal Smoke Assertions

- Plugin appears in server plugin list.
- Core command registrations exist (`tp`, `rtp`, `tpa`, `home`, `team`, `warp`).
- GUI root menu can be opened by command.
- Cobblestone conversion command path succeeds for an operator test account.
- Hotbar slot `8` menu entrypoint opens GUI and cannot be dropped.
- Scoreboard sidebar appears with online-count and points lines.
