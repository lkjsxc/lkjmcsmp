# Command Contracts

## Goal

Specify canonical command behavior, permissions, side effects, and error outputs.

## Rules

1. Every GUI action has a command-equivalent path.
2. Commands return explicit success or explicit failure; no silent outcomes.
3. Permission failures include the missing permission node.
4. Cooldowns and delays include remaining time where relevant.
5. GUI interactions must not rely on left-click/right-click divergence for command intent.
6. Growth-heavy command-backed lists must preserve stable pagination ordering in GUI.

## Child Index

- [teleport.md](teleport.md): direct/request/random teleport contracts and routing
- [homes-warps.md](homes-warps.md): home and warp lifecycle contracts
- [party.md](party.md): party command behavior and ownership rules
- [utility.md](utility.md): `/menu`, `/points`, `/convert`, `/shop`, and `/adv`
