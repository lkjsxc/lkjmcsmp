# Achievement Contracts

## Goal

Define achievement definitions, progress tracking, and reward claim semantics.

## Rules

1. Achievement states must be explicit and transition-safe.
2. Reward claims must be replay-safe and idempotent.
3. Progress visibility in commands/GUI must match stored state.
4. Achievement set should cover multiple gameplay domains (economy, homes/warps, social, teleport, shop).
5. Shop achievement thresholds count purchased item quantities, not multiplier units.
6. Include one cumulative cobblestone-conversion achievement with deterministic target and rewards.

## Child Index

- [achievement-model.md](achievement-model.md): status model and unlock rules
- [reward-hooks.md](reward-hooks.md): reward side effects and replay safety
