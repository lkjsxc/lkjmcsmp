# Scoreboard Failure Model

## Goal

Define explicit failure handling and recovery rules for sidebar rendering.

## Failure Classes

1. **Data Failure**
   - Points lookup fails.
   - Handling: log warning and continue with fallback `points=0`.
2. **Mutation Failure**
   - Scoreboard API mutation throws or fails verification.
   - Handling: schedule bounded retry with cleanup-first rebuild.
3. **Lifecycle Race**
   - Player offline, stale epoch, plugin stopping, or scoreboard manager unavailable.
   - Handling: abort current attempt safely; no success-shaped fallback.
4. **Exhausted Recovery**
   - Retry budget consumed with no successful render.
   - Handling: mark player degraded and log structured error.

## Retry Policy

1. Retry delays (ticks): `20`, `100`, `200`.
2. Maximum attempts per trigger path: `3`.
3. Every retry performs cleanup + full rebuild before verification.
4. Retry execution must validate current epoch and online state before mutation.

## Logging Contract

All mutation failures include:

1. `trigger`
2. `playerUuid`
3. `attempt`
4. concise failure summary

Exhausted retries emit `ERROR` severity. Successful recovery from degraded state emits recovery info log.

## Degraded State Contract

1. Player enters degraded state only after retry budget exhaustion.
2. Degraded players stay eligible for the next periodic reconcile attempt.
3. Any successful render clears degraded state immediately.
4. Degraded state is in-memory runtime state and is cleared on stop/quit teardown.

## Regression Conditions

1. Online player remains without sidebar after retry + reconcile window.
2. Missing structured retry logs for mutation failures.
3. Recovery path does not reclaim `DisplaySlot.SIDEBAR`.
4. Data failures hide sidebar output instead of rendering fallback values.
