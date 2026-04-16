# Runtime Architecture

## Goal

Describe runtime package responsibilities and plugin lifecycle orchestration.

## Rules

1. Composition root logic stays in plugin wiring packages.
2. Domain services own gameplay policy, not transport or event wiring.
3. Lifecycle failures are explicit and fail fast.

## Child Index

- [module-map.md](module-map.md): package-level ownership map
- [lifecycle.md](lifecycle.md): enable/disable and startup failure contracts
