# Scheduler Contract

## Goal

Route gameplay actions through Folia-safe schedulers without exposing scheduler complexity to domain services.

## Bridge API Expectations

- `runPlayerTask(Player, Runnable)`
- `runRegionTask(Location, Runnable)`
- `runAsyncTask(Runnable)`
- `runPlayerDelayedTask(Player, long delayTicks, Runnable)`
- completion-aware player teleport adapter (async completion with explicit success/failure)

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
8. Any player-facing teleport result is emitted only from completion callbacks, never pre-teleport.
9. Delayed teleport stability checks use player-safe delayed scheduling and must re-check online state.
10. Scoreboard periodic reconcile uses player-scoped delayed scheduling; do not depend on global tick orchestration.
11. Scoreboard online-count rendering data is supplied via tracked join/quit state, not cross-player scans in player mutation tasks.
12. Scoreboard retry scheduling uses the same player-scoped delayed scheduling as periodic reconcile.
13. Scoreboard render attempts verify player online state and render epoch freshness before mutation work.
14. Scoreboard snapshot loading runs async; only snapshot application mutates Bukkit scoreboard state.
