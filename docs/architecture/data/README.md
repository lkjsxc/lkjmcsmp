# Data Contracts

## Goal

Define SQLite schema, normalization rules, and consistency guarantees.

## Rules

1. Schema and DAO behavior remain synchronized.
2. Mutating operations define transactional expectations.
3. Data integrity failures are explicit and non-silent.

## Child Index

- [sqlite-schema.md](sqlite-schema.md): canonical table and column contracts
- [consistency.md](consistency.md): integrity and mutation invariants
