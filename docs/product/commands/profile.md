# Profile Command Contract

## Goal

Expose a single command that opens the player's own profile menu.

## Command

- `/profile`
- Permission: `lkjmcsmp.profile.use`
- Default: `true`

## Behavior

1. Opens the profile GUI immediately.
2. No arguments are accepted.
3. Fails with explicit message if the menu cannot be opened.

## Cross-References

- [../features/profile.md](../features/profile.md): menu layout and contents
