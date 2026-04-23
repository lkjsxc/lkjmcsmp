# Death Drop Prevention Contract

## Goal

Ensure the slot-8 menu token (Nether Star) is never lost on player death.

## Rules

1. On `PlayerDeathEvent`, the plugin iterates `event.getDrops()`.
2. Any item matching `HotbarMenuService.isToken(item)` is removed from the drops list.
3. The token is reinstalled in slot 8 on respawn via `PlayerRespawnEvent`.
4. No other drops are affected.

## Failure Contract

1. If the token is missing after respawn, `HotbarMenuListener.onRespawn` reinstalls it immediately.
2. If drop removal fails, the token remains in the death drops and can be picked up by other players; this is treated as a bug.

## Cross-References

- [../gui/hotbar-entrypoint.md](../gui/hotbar-entrypoint.md): slot-8 lock and respawn restoration
