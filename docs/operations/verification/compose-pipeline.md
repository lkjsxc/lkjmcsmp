# Compose Verification Pipeline

## Canonical Commands

```bash
docker compose -f docker-compose.yml -f docker-compose.verify.yml build verify smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm verify
docker compose -f docker-compose.verify.yml run --rm verify
docker compose -f docker-compose.yml -f docker-compose.verify.yml up -d folia
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml down -v
```

## Required Behavior

1. `verify` runs build, tests, docs topology checks, and line-limit checks.
2. `smoke` runs scripted command checks against a live Folia container.
3. Non-zero from any step blocks acceptance.
4. Final `down -v` removes test state.
5. `docker-compose.verify.yml` must be valid by itself for the `verify` service.
6. Verification expectations include:
    - slot-8 hotbar open reliability in blocked/cancelled interaction contexts
    - homes add-current and dedicated deletion flow
    - shop list-to-detail purchase behavior with direct final-quantity (`1`, `2`, `4`, `8`, `16`, `32`, `64`) semantics
    - in-menu points balance visibility on shop detail
    - pagination behavior in growth-heavy menus
    - multi-request `/tpaccept` picker behavior
    - picker-menu manual refresh visibility with no background auto-refresh reopen loop
    - team disband confirm screen flow and post-action refresh
    - GUI slot-map alignment markers for shop-detail direct-buy cluster
    - metadata-based GUI action routing and dynamic language registry markers
    - current-world-spawn respawn RTP marker
    - repeatable all-arrival initial trigger RTP marker
    - action-bar idle/teleport/combat contract markers with always-on guarantee

## Stop Rule

- No failing compose gate may be ignored for merge acceptance.
