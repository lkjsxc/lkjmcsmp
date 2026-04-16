# Teleport Command Contract

## Goal

Provide reliable player movement utilities with clear limits and Folia-safe execution.

## Commands

- `/tp <player>`
- `/tpaccept`
- `/tpdeny`
- `/tpa <player>`
- `/tpahere <player>`
- `/rtp [world]`

## Permissions

- `lkjmcsmp.tp.use`
- `lkjmcsmp.tpa.use`
- `lkjmcsmp.rtp.use`
- `lkjmcsmp.rtp.bypasscooldown`

## Rules

1. `tpa` requests expire after configured timeout.
2. Requests are one-to-one; newest request replaces previous same-direction request.
3. `rtp` requires world whitelist membership.
4. `rtp` enforces cooldown unless bypass permission is present.
5. `rtp` location validation is border-only under current contract.
6. Teleports execute using Folia-safe scheduling for source and target entities.

## Failures

- Offline target: explicit failure.
- Missing permission: explicit node in message.
- Cooldown active: remaining seconds included.
- No valid world available: explicit failure.
