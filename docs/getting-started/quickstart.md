# Quickstart

## Goal

Boot `lkjmcsmp` build and tests with the minimum canonical command set.

## Prerequisites

- Docker and Docker Compose plugin available on host
- Git checkout of this repository

## Commands

```bash
docker compose -f docker-compose.yml -f docker-compose.verify.yml build verify
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm verify
```

Expected:

- Build succeeds.
- Unit and integration tests pass.
- Docs and line-limit validators pass.

## Optional Folia Smoke Verification

```bash
docker compose -f docker-compose.yml -f docker-compose.verify.yml up -d folia
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm smoke
docker compose -f docker-compose.yml -f docker-compose.verify.yml down -v
```

Expected:

- Folia boots with plugin enabled.
- Scripted command checks pass.
