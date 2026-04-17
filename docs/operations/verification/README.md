# Verification Operations

## Goal

Define acceptance gates and scripted checks that block regressions.

## Rules

1. Non-zero verify/smoke results block acceptance.
2. Verification covers docs topology, line limits, build/test, and runtime smoke.
3. Scripted checks track contracts in product and architecture docs.
4. Scoreboard visibility/recovery failures are always blocker failures.
5. Hotbar entrypoint and menu interaction regressions are blocker failures.

## Child Index

- [compose-pipeline.md](compose-pipeline.md): canonical compose command sequence
- [scripted-checks.md](scripted-checks.md): smoke assertion contract
