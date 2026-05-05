# Initial Trigger RTP Contract

## Goal

Move players out of the configured initial zone only after a visible countdown.

## Trigger Zone

1. The trigger zone is a cylinder on `teleport.initial-trigger.trigger-world`.
2. Horizontal center is `center-x`, `center-z`.
3. Radius is `trigger-radius-blocks`; default is `200`.
4. Y coordinate is ignored for zone membership.
5. The RTP target world is `target-world`; blank means the player's current world.

## Rules

1. The feature replaces immediate first-join RTP.
2. It arms whenever an online valid player is placed inside the trigger zone.
3. Trigger sources are join, respawn, teleport, world change, and movement into a new block.
4. Join, respawn, teleport, and world-change checks run after a short player-safe delay so final placement is evaluated.
5. The feature is repeatable; no per-player completion row suppresses future triggers.
6. A player can have only one active initial-trigger countdown at a time.
7. It displays a countdown using chat and the teleport HUD source.
8. Countdown length is `countdown-seconds`; default is `5`.
9. If `cancel-on-exit=true`, leaving the trigger zone cancels the armed countdown.
10. Offline, dead, invalid, or wrong-world players are disarmed.
11. Countdown completion calls RTP with cooldown bypass and no second stability delay.
12. Failed RTP leaves the player eligible for a future trigger attempt.

## Completion

1. Success is a confirmed teleport completion from the teleport service.
2. Success does not persist a player-level completion record.
3. A later arrival inside the configured zone starts a new countdown.
4. Teleport result chat is sent only after the teleport callback returns.

## Cancellation

1. Leaving the trigger zone cancels only when `cancel-on-exit=true`.
2. Going offline, dying, invalidating, or changing to the wrong world always disarms.
3. Cancellation does not block future trigger attempts.

## Config Keys

- `teleport.initial-trigger.enabled`
- `teleport.initial-trigger.trigger-world`
- `teleport.initial-trigger.target-world`
- `teleport.initial-trigger.center-x`
- `teleport.initial-trigger.center-z`
- `teleport.initial-trigger.trigger-radius-blocks`
- `teleport.initial-trigger.countdown-seconds`
- `teleport.initial-trigger.cancel-on-exit`

## Failure Contract

1. No safe RTP destination: player stays in place and receives explicit failure feedback.
2. Invalid target world: player stays in place and receives explicit failure feedback.
3. Cancellation removes the armed countdown and allows later retrigger.

## Cross-References

- [../commands/teleport.md](../commands/teleport.md): RTP safety and completion rules
- [respawn-rtp.md](respawn-rtp.md): death respawn RTP remains independent
