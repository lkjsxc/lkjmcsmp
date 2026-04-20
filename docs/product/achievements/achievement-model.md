# Achievement Model

## Goal

Maintain achievement progress in plugin state without datapack dependency.

## States

- `LOCKED`
- `IN_PROGRESS`
- `COMPLETED_UNCLAIMED`
- `COMPLETED_CLAIMED`

## Achievement Examples

- First conversion (`convert 64 cobblestone`)
- Cumulative conversion (`convert 10000 cobblestone`)
- First home set
- First warp use
- First party join
- Total points earned thresholds
- First shop purchase
- First teleport request sent
- First random teleport use
- Home count thresholds
- Shop purchase quantity thresholds
- Party participation thresholds

## Metadata Contract

- `title`: short player-facing name
- `description`: detailed explanation of completion criteria and intent
- `kind`: event stream key used for progress updates
- `target`: completion threshold
- `reward-points`: claim reward amount

## Rules

1. Achievements are deterministic and recomputable from events.
2. Completion is idempotent.
3. Claim is one-time unless an achievement is explicitly repeatable.
4. GUI and command surfaces show the same achievement status.
5. GUI must show numeric progress (`current/target`) and percentage.
6. `LOCKED` achievements still show description and target for forward planning.
7. `COMPLETED_UNCLAIMED` achievements must render with clear claim affordance.
8. Growth strategy should prefer many small deterministic achievements over a single sparse chain.
