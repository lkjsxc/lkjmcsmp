# Scheduler Contract

## Goal

Route gameplay actions through Folia-safe schedulers without exposing scheduler complexity to domain services.

## Bridge API Expectations

- `runPlayerTask(Player, Runnable)`
- `runRegionTask(Location, Runnable)`
- `runAsyncTask(Runnable)`
- `runPlayerDelayedTask(Player, long delayTicks, Runnable)`
- `runGlobalDelayedTask(long delayTicks, Runnable)`
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
10. **HUD periodic re-evaluation** runs on per-player periodic tasks (`2` tick interval); no global tick orchestration.
11. Idle HUD inputs (cumulative playtime and online count) are refreshed from player-safe state-change events.
12. HUD state transitions verify player online state before mutation work.
13. **Task Cancellation**: scheduled delayed tasks owned by the plugin must be cancellable. On plugin disable, all recurring or delayed tasks must be stopped to prevent duplication on reload.
