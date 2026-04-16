# Command Contracts

## Goal

Specify canonical command behavior, permissions, side effects, and error outputs.

## Rules

1. Every GUI action has a command-equivalent path.
2. Commands return explicit success or explicit failure; no silent outcomes.
3. Permission failures include the missing permission node.
4. Cooldowns and delays include remaining time where relevant.

## Child Index

- [teleport.md](teleport.md): direct/request/random teleport contracts and routing
- [homes-warps.md](homes-warps.md): home and warp lifecycle contracts
- [party.md](party.md): party command behavior and ownership rules
- [utility.md](utility.md): `/menu`, `/points`, `/convert`, `/shop`, and `/adv`
