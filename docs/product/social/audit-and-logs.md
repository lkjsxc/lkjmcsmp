# Audit and Logs

## Goal

Capture high-impact economy and social mutations for traceability.

## Required Audit Events

- Economy seasonal override create/update/delete
- Operator point balance adjustments
- Party disband events
- Milestone reward claim failures

## Required Fields

- actor UUID or `SYSTEM`
- target UUID (when applicable)
- event key
- before and after payload
- timestamp (UTC ISO-8601)

## Retention Rules

1. Keep audit records in SQLite until manual prune.
2. Prune operation requires explicit command and permission.
3. Prune action itself is audit logged.
