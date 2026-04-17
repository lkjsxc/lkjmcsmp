# Operations

## Goal

Define operational runbooks for deployment, verification, and acceptance gates.

## Rules

1. Compose verification is the mandatory acceptance path.
2. Local runtime guidance stays aligned with deployment contracts.
3. Operational docs use command-first, copy-paste-friendly steps.
4. Smoke assertions must match documented blocker contracts.
5. Verification assertions must track shop quantity semantics, picker refresh policy, and scoreboard recovery guarantees.

## Child Index

- [verification/README.md](verification/README.md): compose pipeline and smoke assertions
- [deployment/README.md](deployment/README.md): local server and configuration operations
