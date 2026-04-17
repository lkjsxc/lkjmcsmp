# Folia Contracts

## Goal

Define Folia scheduler usage and thread-safety constraints for all gameplay paths.

## Rules

1. Player, region, and async execution contexts are chosen explicitly.
2. Teleport and GUI mutations run only in safe player/region contexts.
3. Cross-thread state handoff uses explicit contracts and failure reporting.
4. Scoreboard reconcile loops are player-scoped and do not depend on global tick orchestration.

## Child Index

- [scheduler-contract.md](scheduler-contract.md): scheduler bridge and callback contracts
- [thread-safety-rules.md](thread-safety-rules.md): Bukkit API thread-safety boundaries
