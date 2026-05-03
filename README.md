# lkjmcsmp

Folia-compatible SMP utility plugin with GUI-first operations, cobblestone-backed points economy, lightweight party system, and plugin-managed achievements.

## Quickstart

```bash
./gradlew --no-daemon clean test shadowJar
docker compose -f docker-compose.yml -f docker-compose.verify.yml run --rm verify
docker compose -f docker-compose.yml -f docker-compose.verify.yml up --build smoke
```

## Documentation

The canonical documentation lives in [docs/README.md](docs/README.md).

## Core Principles

1. **Docs-first**: update contracts before implementation.
2. **Deterministic verification**: compose pipeline gates acceptance.
3. **Small files**: docs `<= 300` lines, source `<= 200` lines.
4. **Folia-safe**: all gameplay mutations use scheduler bridge.

## Key Systems

- **Economy**: Cobblestone Points from cobblestone conversion.
- **Teleport**: `/tp`, `/tpa`, `/tpahere`, `/rtp`, and initial trigger-zone RTP.
- **Homes/Warps**: GUI-first with pagination.
- **Party**: lightweight teams with invites and homes.
- **Shop**: list-to-detail purchase with direct quantity buttons.
- **Achievements**: plugin-managed progress and rewards.
- **Temporary End**: purchasable End dimension instances.
- **HUD**: action-bar idle, teleport, and combat overlays.

## Technology

- Java 21
- Paper API 1.21.1
- Folia region schedulers
- SQLite persistence
- Shadow plugin for fat JAR
