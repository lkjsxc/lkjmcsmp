# Temporary End Dimension Contracts

## Goal

Define the temporary End-like dimension instance purchased with accumulated cobblestone points.

## Rules

1. Each purchase creates an independent, isolated End dimension instance.
2. Multiple instances may exist simultaneously.
3. Instances have a strict lifecycle with deterministic cleanup.
4. Player safety on expiry is mandatory; no stranding in deleted dimensions.

## Child Index

- [purchase-and-creation.md](purchase-and-creation.md): cost, trigger, and player transfer
- [lifecycle.md](lifecycle.md): states, duration, expiry, and recovery
- [access-rules.md](access-rules.md): gameplay behavior inside temporary End instances
- [admin-commands.md](admin-commands.md): operator visibility and controls
