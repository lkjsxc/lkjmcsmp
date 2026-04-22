# Temporary End Access Rules

## Vanilla End Behavior

1. Beds explode when used.
2. Respawn anchors explode when used.
3. Respawn follows normal bed/spawn behavior (overworld spawn if no bed).

## Ender Dragon

1. Each temporary End instance spawns its own Ender Dragon.
2. Dragon kill behaves per-world; loot and experience drop normally.
3. Exit portal generates normally upon dragon death.

## Portals

1. Exit portal behavior is vanilla: returns player to the main overworld spawn.
2. No custom portal redirection is implemented.

## Loot and Persistence

1. Chunks, chests, and placed blocks persist only while the instance is active.
2. On expiry the world is deleted (`save = false`); nothing persists.
3. Items in player inventories remain with the player.

## Elytra and End Cities

1. End cities and End ships generate as part of vanilla End generation.
2. Elytra discovery is possible and behaves normally.
