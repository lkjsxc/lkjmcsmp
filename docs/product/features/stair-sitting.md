# Stair Sitting Contract

## Summary

Players can sit on stair blocks through a lightweight seat entity that is cleaned up deterministically.

## Input Rules

1. Trigger: right-click a stair block with an empty main hand.
2. Requires permission `lkjmcsmp.sit.stairs`.
3. Sneaking bypasses sitting and allows normal block interaction.
4. Occupied stairs reject the action with a short message.
5. Non-stair blocks and non-empty-hand interactions are ignored.

## Seat Behavior

1. The plugin spawns an invisible marker ArmorStand at the stair center.
2. The player is mounted as the only passenger.
3. The seat is scoped to the stair block location.
4. One player may occupy a stair seat at a time.
5. Sitting does not modify the stair block.

## Cleanup Rules

1. Remove the seat on dismount.
2. Remove the seat on player quit, death, teleport, or world change.
3. Remove the seat if the stair block is broken.
4. Remove all seats on plugin disable.
5. Cleanup must not leave visible entities or passengers behind.

## Cross-References

- [README.md](README.md): cross-cutting feature index
- [../../architecture/folia/scheduler-contract.md](../../architecture/folia/scheduler-contract.md): player-safe entity mutation
