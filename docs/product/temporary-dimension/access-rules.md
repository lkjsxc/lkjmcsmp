# Temporary Dimension Access Rules

## Summary

Gameplay behavior inside a temporary dimension depends on its environment type.

## THE_END Behavior

1. Beds explode when used.
2. Respawn anchors explode when used.
3. Respawn follows normal bed/spawn behavior (overworld spawn if no bed).
4. Each instance spawns its own Ender Dragon.
5. Dragon kill behaves per-world; loot and experience drop normally.
6. Exit portal may generate normally upon dragon death.
7. Exit portal use is cancelled by the common portal rule.
8. End cities and End ships generate as part of vanilla End generation.
9. Elytra discovery is possible and behaves normally.

## NETHER Behavior

1. Beds explode when used.
2. Respawn anchors function normally.
3. Nether fortresses and bastions generate as part of vanilla Nether generation.
4. Portal use is cancelled by the common portal rule.

## NORMAL Behavior

1. Normal overworld terrain generation.
2. No special restrictions; behaves like a standard overworld.

## Common Loot and Persistence Rules

1. Chunks, chests, and placed blocks persist only while the instance is active.
2. On expiry the world is deleted (`save = false`); nothing persists.
3. Items in player inventories remain with the player.

## Portal and Escape Rules

1. `PlayerPortalEvent` is cancelled inside all temporary dimensions to prevent main-world network linkage.
2. Death respawn is redirected to the participant's recorded origin.

## Cross-References

- [lifecycle.md](lifecycle.md): when instances expire and what happens to players
- [purchase-and-creation.md](purchase-and-creation.md): creation and transfer rules
