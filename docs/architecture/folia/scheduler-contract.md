# Scheduler Contract

## Goal

Route gameplay actions through Folia-safe schedulers without exposing scheduler complexity to domain services.

## Bridge API Expectations

- `runPlayerTask(Player, Runnable)`
- `runGlobalTask(Runnable)`
- `runRegionTask(Location, Runnable)`
- `runDelayedPlayerTask(Player, ticks, Runnable)`

## Rules

1. Teleports execute in player or region-safe context.
2. GUI open and inventory mutation execute on player-safe context.
3. Persistence writes run off the main gameplay execution path.
4. Command handlers must not block scheduler threads on DB I/O.
