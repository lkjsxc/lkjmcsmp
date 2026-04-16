# Scheduler Contract

## Goal

Route gameplay actions through Folia-safe schedulers without exposing scheduler complexity to domain services.

## Bridge API Expectations

- `runPlayerTask(Player, Runnable)`
- `runRegionTask(Location, Runnable)`
- `runAsyncTask(Runnable)`

## Rules

1. Teleports execute in player or region-safe context.
2. GUI open and inventory mutation execute on player-safe context.
3. Persistence writes run off the main gameplay execution path.
4. Command handlers must not block scheduler threads on DB I/O.
5. Cross-player teleports use two-phase handoff:
   - capture source location in source-safe context
   - execute teleport in destination player-safe context
6. RTP terrain probing (`highest block`, safety blocks) executes in region-safe context.
7. Completion/failure callbacks must surface explicit player-facing results.
