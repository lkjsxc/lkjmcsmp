# Progression Contracts

## Goal

Define pseudo-advancement milestones, progress tracking, and reward claim semantics.

## Rules

1. Milestone states must be explicit and transition-safe.
2. Reward claims must be replay-safe and idempotent.
3. Progress visibility in commands/GUI must match milestone state.
4. Milestone set should cover multiple gameplay domains (economy, homes/warps, social, teleport, shop).
5. Shop progression thresholds count purchased item quantities, not multiplier units.

## Child Index

- [progression-model.md](progression-model.md): status model and unlock rules
- [reward-hooks.md](reward-hooks.md): reward side effects and replay safety
