# Initial Trigger RTP Contract

## Goal

Move new players out of the protected initial zone only after a visible countdown.

## Trigger Zone

1. The trigger zone is a cylinder on `teleport.initial-trigger.trigger-world`.
2. Horizontal center is `center-x`, `center-z`.
3. Radius is `trigger-radius-blocks`; default is `200`.
4. Y coordinate is ignored for zone membership.
5. The RTP target world is `target-world`; blank means the player's current world.

## Rules

1. The feature replaces immediate first-join RTP.
2. It arms on player join or movement when the player enters the trigger zone.
3. It applies only when `once-per-player=true` and the player has no completion row.
4. It displays a countdown using chat and the teleport HUD source.
5. Countdown length is `countdown-seconds`; default is `5`.
6. If `cancel-on-exit=true`, leaving the trigger zone cancels the armed countdown.
7. Offline, dead, invalid, or wrong-world players are disarmed.
8. Countdown completion calls RTP with cooldown bypass and no second stability delay.
9. Completion is persisted only after RTP success is confirmed.
10. Failed RTP leaves the player eligible for a future trigger attempt.

## Config Keys

- `teleport.initial-trigger.enabled`
- `teleport.initial-trigger.trigger-world`
- `teleport.initial-trigger.target-world`
- `teleport.initial-trigger.center-x`
- `teleport.initial-trigger.center-z`
- `teleport.initial-trigger.trigger-radius-blocks`
- `teleport.initial-trigger.countdown-seconds`
- `teleport.initial-trigger.cancel-on-exit`
- `teleport.initial-trigger.once-per-player`

## Failure Contract

1. No safe RTP destination: player stays in place and receives explicit failure feedback.
2. Invalid target world: player stays in place and receives explicit failure feedback.
3. Cancellation removes the armed countdown but does not persist completion.

## Cross-References

- [../commands/teleport.md](../commands/teleport.md): RTP safety and completion rules
- [respawn-rtp.md](respawn-rtp.md): death respawn RTP remains independent
