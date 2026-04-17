# Compose Verification Pipeline

## Canonical Commands

```bash
docker compose -f docker-compose.yml -f docker-compose.verify.yml build verify smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm verify
docker compose -f docker-compose.yml -f docker-compose.verify.yml up -d folia
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml down -v
```

## Required Behavior

1. `verify` runs build, tests, docs topology checks, and line-limit checks.
2. `smoke` runs scripted command checks against a live Folia container.
3. Non-zero from any step blocks acceptance.
4. Final `down -v` removes test state.
5. Verification expectations include:
    - slot-8 hotbar open reliability in blocked/cancelled interaction contexts
    - homes add-current and dedicated deletion flow
    - shop list-to-detail purchase behavior with final-quantity (`1..64`) semantics
    - pagination behavior in growth-heavy menus
    - multi-request `/tpaccept` picker behavior
    - picker-menu manual refresh visibility with no background auto-refresh reopen loop
    - GUI slot-map alignment markers for shop-detail control cluster
    - scoreboard visibility and recovery with sidebar ownership reclaim
    - runtime scoreboard overwrite/removal injection probe via `/lkjverify scoreboard`

## Stop Rule

- No failing compose gate may be ignored for merge acceptance.
