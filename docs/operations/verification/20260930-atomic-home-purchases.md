# Verification: Atomic Home Purchases (2026-09-30 JST)

## Source Identity and Scope

- Entry: `ef861e42341d7347daa42eb719f8f08c0d32db24`.
- Home implementation and Java tests: `da2ad5dbef6caff05037ae5a7a8a129bd48d8ac7`.
- Final executable verification source: `b8435955b2a174a20d1a80d2a5832ef8821db2c3`, tree `a8b002a03d6cd0838e54f0369d5e5c8312bbd31c`.
- Home and RCON contracts were committed before their respective implementations. The closeout adds documentation only.
- Work ran in the shared development workspace, using a newly cloned checkout of this repository. No production plugin, world, balance, or server configuration was changed.

## Failure Reproduction

Before changing production code, two new service tests were run against the entry implementation:
`failedSlotWriteLeavesAllPurchaseTablesUnchanged` and
`failedCommitLeavesAllPurchaseTablesUnchanged`.

Both failed. The old flow committed a debit before the slot mutation and then committed a compensating refund after the injected slot/commit failure. Its full three-table snapshot differed from the pre-purchase state. This also exposed a process-stop window between independent commits; a process-kill or power-loss experiment was not performed.

The same tests pass with the single-connection purchase transaction.

## Executed Gates

| Gate | Observed result |
| --- | --- |
| Native Java 21.0.12.1, Gradle wrapper 8.10.2: clean test + shadowJar | Passed |
| Compose `gradle:8.10.2-jdk21`, canonical `scripts/verify.sh` | Passed |
| Full JUnit suite | 65 cases, 20 classes; zero failures, errors, or skips |
| Offline RCON response validator | 7 tests passed, both directly and in verify/smoke |
| Concurrent purchase suite with `--rerun-tasks` | 3 additional consecutive successful runs; both journal modes in each |
| Documentation topology and file line limits | Passed |
| `git diff --check` | Passed |
| Isolated Folia boot and tightened RCON smoke | Passed; no host-published ports |

Existing compiler/Gradle deprecation warnings remain; they were not hidden or treated as compatibility certification.

An initial native concurrency rerun failed before tests because a root-running Compose build had produced root-owned build outputs. Repository-local generated output ownership was repaired. The three successful repeated runs were then executed inside the verification container. This was an environment failure, not a passing test or a concurrency assertion failure.

## SQLite Evidence

The tests use actual temporary SQLite databases, not mocked transaction success.

Both `DELETE` and `WAL` journal modes cover all 21 catalog upgrades, their exact prices and ledger metadata, exhausted catalogs, stale/future/invalid expected slots, and insufficient balances including a missing balance row.

Fault injection covers slot insertion, slot update, balance update, ledger insertion, and commit. The commit case uses a deferred foreign-key violation; the other cases use aborting triggers. After each failure, every row in `player_points`, `player_home_slots`, and `points_ledger`, including timestamps, equals its pre-purchase snapshot. Removing the failure trigger allows an explicit retry to succeed once.

Eight simultaneous requests for the same expected slot produce one purchase and seven stale-order results. A Home purchase racing another debit cannot both spend a 600-Point balance. Connection-scoped mutation helpers reject auto-commit connections without changing storage.

No schema migration, catalog repricing, or historical ledger rewrite is introduced. Separate deliberate purchases may still buy consecutive slots; this change does not add a user-request idempotency key.

## RCON Evidence and Proof Ceiling

The old smoke printed success for `help lkjmcsmp:tp` even when the server returned `No help for lkjmcsmp:tp`. Matching the echoed command name was not proof of success.

The new validator removes ANSI/Minecraft formatting and rejects empty, missing-help, unknown-command, permission-denied, and internal-error responses. Namespaced `lkjmcsmp:tp` and `lkjmcsmp:home` are dispatched directly; both return the plugin's player-only guard from the console. Normal help checks and source-marker checks also pass.

The disposable server reported:
`Folia 1.21.11-14-ver/1.21.11@529aabc`, API `1.21.11-R0.1-SNAPSHOT`.
Logs confirmed `lkjmcsmp enabled`. The final smoke command exited zero. Its server/container/network were stopped and removed afterwards.

This establishes plugin boot, structural checks, and console dispatch. It does not establish real-player GUI behavior, multi-region safety, temporary End creation/evacuation/deletion, production readiness, or Folia 26.1.2 compatibility.

## Reproduction

Use a disposable checkout; the base Compose file mounts `./tmp/mc-data`. Never point this verification at a production data directory.

```sh
docker compose -p lkjmcsmp-check -f docker-compose.verify.yml run --rm verify
python3 -B -m unittest discover -s scripts -p 'test_smoke_response.py'
```

The smoke run used Docker Compose 2.40.3 and this extra override file:

```yaml
services:
  folia:
    ports: !reset []
```

Save it as `/tmp/lkjmcsmp-smoke-isolated.yml`, confirm that the merged configuration publishes no ports, then run:

```sh
docker compose -p lkjmcsmp-check -f docker-compose.yml \
  -f /tmp/lkjmcsmp-smoke-isolated.yml run --rm smoke
docker compose -p lkjmcsmp-check -f docker-compose.yml \
  -f /tmp/lkjmcsmp-smoke-isolated.yml down --timeout 30
```

The unmodified base Compose configuration remains a development convenience, not a hardened public deployment. No automatic CI workflow, release tag, or production deployment was added in this checkpoint.

## Next Boundary

See [the engineering roadmap](../../vision/engineering-roadmap.md). The next product priority is durable recovery for inventory conversion and asynchronous paid services, followed by version-specific temporary-dimension and real-player acceptance.
