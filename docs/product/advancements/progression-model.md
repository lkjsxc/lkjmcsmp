# Pseudo-Advancement Model

## Goal

Maintain progression milestones in plugin state without datapack dependency.

## Milestone States

- `LOCKED`
- `IN_PROGRESS`
- `COMPLETED_UNCLAIMED`
- `COMPLETED_CLAIMED`

## Milestone Examples

- First conversion (`convert 64 cobblestone`)
- First home set
- First warp use
- First party join
- Total points earned thresholds

## Rules

1. Milestones are deterministic and recomputable from events.
2. Completion is idempotent.
3. Claim is one-time unless milestone is explicitly repeatable.
4. GUI and command surfaces show same milestone status.
