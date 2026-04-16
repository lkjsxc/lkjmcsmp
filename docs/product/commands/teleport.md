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

## Routing Rules

1. Plain `/tp` must prefer plugin behavior (`lkjmcsmp`) for player-issued commands where server command-map constraints allow it.
2. Namespaced fallback `/lkjmcsmp:tp` must always execute plugin behavior.
3. Teleport command handlers must route by canonical command name, not raw label text, so namespaced dispatch remains stable.

## Permissions

- `lkjmcsmp.tp.use`
- `lkjmcsmp.tpa.use`
- `lkjmcsmp.rtp.use`
- `lkjmcsmp.rtp.bypasscooldown`

## RTP Config Contract

- `teleport.rtp-min-radius`
- `teleport.rtp-max-radius`
- `teleport.rtp-attempts`
- `teleport.rtp-world-whitelist`
- `teleport.rtp-cooldown-seconds`

## RTP Default Contract

- Default min radius: `1000`
- Default max radius: `100000`

## Teleport Rules

1. `tpa` requests expire after configured timeout.
2. Requests are one-to-one; newest request replaces previous same-direction request.
3. `rtp` requires world whitelist membership.
4. `rtp` enforces cooldown unless bypass permission is present.
5. `rtp` selects random locations using configured donut range (`min/max radius`).
6. `rtp` uses bounded attempts and fails explicitly when no valid location is found.
7. `rtp` location validation requires safe feet/head space and non-lethal support block.
8. Teleports execute using Folia-safe scheduling for source and target entities and completion-driven teleport API.
9. Success messages are emitted only after teleport completion is confirmed.
10. Failed teleports must return explicit reason; no success-shaped fallback responses.
11. `/home`, `/warp`, and `/team home` teleport outcomes follow the same completion/failure semantics as `/tp` and `/rtp`.

## First-Join RTP Rules

1. First-join RTP runs once per player UUID globally.
2. First-join RTP bypasses cooldown checks only.
3. First-join RTP still obeys whitelist, range, attempts, and safety rules.
4. If no valid location is found, player stays at spawn and receives explicit failure feedback.

## Failures

- Offline target: explicit failure.
- Missing permission: explicit node in message.
- Cooldown active: remaining seconds included.
- No valid world available: explicit failure.
- No safe RTP location after max attempts: explicit failure.
- Scheduler/teleport completion failure: explicit failure.
