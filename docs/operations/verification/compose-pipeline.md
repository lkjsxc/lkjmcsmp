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

## Stop Rule

- No failing compose gate may be ignored for merge acceptance.
