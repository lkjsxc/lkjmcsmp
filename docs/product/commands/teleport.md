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

## Config Contract

### RTP Keys

- `teleport.rtp-min-radius`
- `teleport.rtp-max-radius`
- `teleport.rtp-attempts`
- `teleport.rtp-world-whitelist`
- `teleport.rtp-cooldown-seconds`

### Stability Keys

- `teleport.stability-delay-seconds`
- `teleport.stability-radius-blocks`

## Default Contract

- Default min radius: `1000`
- Default max radius: `100000`
- Default stability delay: `5`
- Default stability radius: `1`

## Teleport Rules

1. `tpa` requests expire after configured timeout.
2. Requests are tracked per target with one active request per requester-target pair; newest request from the same requester replaces their previous pending request.
3. `tpa`/`tpahere` request creation notifies both players:
   - requester receives request-created result
   - target receives requester name, direction (`tpa` or `tpahere`), timeout, accept/deny hint, and a clickable chat action that opens the request decision screen
4. `/tpaccept` request resolution:
   - no pending requests: explicit failure
   - one pending request: accept immediately without picker
   - two or more pending requests: open requester-picker GUI (from command or Teleport menu) and accept selected request
   - requester picker lists pending requesters in stable order and exposes manual `Refresh`
5. Request deny/expire/accept outcomes send explicit feedback to all affected players.
6. `rtp` requires world whitelist membership.
7. `rtp` enforces cooldown unless bypass permission is present.
8. `rtp` selects random locations using configured donut range (`min/max radius`).
9. `rtp` uses bounded attempts and fails explicitly when no valid location is found.
10. `rtp` location validation requires safe feet/head space and non-lethal support block.
11. Teleports execute using Folia-safe scheduling for source and target entities and completion-driven teleport API.
12. Success messages are emitted only after teleport completion is confirmed.
13. Failed teleports must return explicit reason; no success-shaped fallback responses.
14. `/home`, `/warp`, and `/team home` teleport outcomes follow the same completion/failure semantics as `/tp` and `/rtp`.
15. Action bar emits teleport state changes in parallel with chat messaging.
16. Action bar countdown text includes remaining seconds during stability delay.
17. Action bar completion/cancellation/failure events are explicit and state-change-driven.
18. The request decision screen is the same requester-picker contract used by `/tpaccept` when more than one pending request exists.
19. A clicked notification action opens the decision screen rather than accepting by accident.
20. The request decision screen for a clicked request shows requester details, Accept, Deny, and Back.
21. Accept/Deny actions resolve the requester UUID encoded in the clicked notification.

## Initial Trigger RTP Rules

1. Initial trigger RTP is owned by [../features/initial-trigger-rtp.md](../features/initial-trigger-rtp.md).
2. Trigger RTP bypasses cooldown and does not apply a second stability delay after its own countdown.
3. Trigger RTP still obeys whitelist, range, attempts, and safety rules.
4. If no valid location is found, player stays in place and receives explicit failure feedback.

## Stability Delay Rules

1. Stability delay applies to all command-driven teleports:
   - `/tp`
   - `/tpa` + `/tpaccept`
   - `/tpahere` + `/tpaccept`
   - `/home`
   - `/warp`
   - `/team home`
   - `/rtp`
2. Delay uses `teleport.stability-delay-seconds`.
3. Movement tolerance uses `teleport.stability-radius-blocks`.
4. The player who will be teleported must remain within configured radius for the full delay window.
5. Distance check is full 3D Euclidean distance from command invocation position.
6. If movement exceeds radius before delay completion, teleport is cancelled with explicit message.
7. Stability countdown messages include remaining seconds.
8. Zero delay disables waiting but still uses normal completion/failure messaging.
9. Countdown action-bar updates are event-driven on second transitions, not global periodic idle ticks.

## Failures

- Offline target: explicit failure.
- Missing permission: explicit node in message.
- Cooldown active: remaining seconds included.
- No valid world available: explicit failure.
- No safe RTP location after max attempts: explicit failure.
- Stability check failed due movement: explicit cancellation.
- Request target offline at request time: explicit failure.
- Request requester offline at accept time: explicit failure.
- Scheduler/teleport completion failure: explicit failure.
