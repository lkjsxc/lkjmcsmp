# Verification Operations

## Goal

Define acceptance gates and scripted checks that block regressions.

## Rules

1. Non-zero verify/smoke results block acceptance.
2. Verification covers docs topology, line limits, build/test, and runtime smoke.
3. Scripted checks track contracts in product and architecture docs.

## Child Index

- [compose-pipeline.md](compose-pipeline.md): canonical compose command sequence
- [scripted-checks.md](scripted-checks.md): smoke assertion contract
