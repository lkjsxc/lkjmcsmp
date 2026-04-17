# Architecture

## Goal

Define module boundaries, persistence contracts, and Folia-safe execution rules.

## Rules

1. Runtime composition, domain policies, and persistence boundaries must remain explicit.
2. Folia scheduling constraints are treated as hard safety contracts.
3. Data consistency rules are documented before schema or DAO changes.
4. Player-scoped lifecycle loops are preferred over global tick assumptions for Folia runtime behavior.

## Child Index

- [runtime/README.md](runtime/README.md): package ownership and lifecycle contracts
- [data/README.md](data/README.md): SQLite schema and consistency expectations
- [folia/README.md](folia/README.md): scheduler and thread-safety constraints
