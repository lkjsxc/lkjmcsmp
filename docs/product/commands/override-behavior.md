# Command Override Contract

## Goal

Ensure `/tp` issued by players resolves to plugin behavior where server command-map constraints allow it.

## Routing Rules

1. `TeleportCommandOverrideListener` intercepts `PlayerCommandPreprocessEvent` for `/tp`.
2. It cancels the original event and dispatches `/lkjmcsmp:tp <args>`.
3. If namespaced dispatch fails, the player receives an explicit diagnostic.
4. Console and command-block `/tp` are not intercepted.

## Fallback Rules

1. If the plugin `/tp` command is missing, the override listener logs a warning.
2. Namespaced `/lkjmcsmp:tp` must always execute plugin behavior when registered.
3. Routing inside `TeleportCommand.onCommand` uses `command.getName()`, not raw label.

## Permissions

- `lkjmcsmp.tp.use` controls access to plugin `/tp`.
- The override listener does not bypass permission checks.
