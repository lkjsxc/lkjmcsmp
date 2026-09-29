# Verification Operations

## Goal

Define acceptance gates and scripted checks that block regressions.

## Rules

1. Non-zero verify/smoke results block acceptance.
2. Verification covers docs topology, line limits, build/test, and runtime smoke.
3. Scripted checks track contracts in product and architecture docs.
4. Action-bar HUD contract failures are always blocker failures.
5. Hotbar entrypoint and menu interaction regressions are blocker failures.
6. Picker refresh visibility and shop final-quantity (`1..64`) semantics are blocker contracts.
7. `/lkjmcsmp` command parity and achievement localization regressions block acceptance.

## RCON Response Gates

1. Normalize ANSI and Minecraft formatting before inspecting English smoke responses.
2. Empty responses and explicit missing-help, unknown-command, permission-denied, or internal-error responses fail the smoke run even if they contain the requested command name.
3. Exercise namespaced `lkjmcsmp:tp` and `lkjmcsmp:home` directly from RCON and require the plugin's player-only guard. A `help` lookup does not prove dispatch and may not index namespaced aliases.
4. Run response-validator unit tests in both `verify.sh` and the smoke entrypoint before network checks.
5. Source-marker checks and console dispatch are not player-interaction, region-safety, or temporary-world lifecycle tests. Report these proof levels separately.

## Child Index

- [compose-pipeline.md](compose-pipeline.md): canonical compose command sequence
- [scripted-checks.md](scripted-checks.md): smoke assertion contract
- [20260930-atomic-home-purchases.md](20260930-atomic-home-purchases.md): atomic purchase and RCON false-positive regression evidence, with explicit limits
