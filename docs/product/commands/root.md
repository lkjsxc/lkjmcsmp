# Root Command Contract

## Goal

Expose menu-equivalent actions through `/lkjmcsmp` with predictable help and tab completion.

## Command

- `/lkjmcsmp [help|menu|home|shop|achievement|profile|settings|language|points|teleport|team]`
- Permission: `lkjmcsmp.menu.use`
- Alias: `/lkj`

## Help Rules

1. `/lkjmcsmp` and `/lkjmcsmp help` print a compact command index.
2. `/lkjmcsmp help <topic>` prints the subcommands for that topic.
3. Help output uses stable command literals and short descriptions.
4. Unknown topics show root help and an explicit unknown-topic line.

## Subcommand Parity

1. `/lkjmcsmp menu` opens the root menu.
2. `/lkjmcsmp home` shows Home help; nested actions are defined in [homes-warps.md](homes-warps.md).
3. `/lkjmcsmp shop` opens the Points Shop.
4. `/lkjmcsmp shop buy <item> [quantity]` matches `/shop buy <item> [quantity]`.
5. `/lkjmcsmp shop convert` converts all cobblestone in the player's inventory.
6. `/lkjmcsmp warps` opens the Warps menu.
7. `/lkjmcsmp warps list` matches `/warps`.
8. `/lkjmcsmp warps go <name>` matches `/warp <name>`.
9. `/lkjmcsmp achievement` opens the achievement menu.
10. `/lkjmcsmp achievement list|claim <key>` matches `/achievement list|claim <key>`.
11. `/lkjmcsmp profile` opens the profile menu.
12. `/lkjmcsmp settings` opens settings.
13. `/lkjmcsmp settings hotbar|actionbar` toggles the matching setting.
14. `/lkjmcsmp language <code>` changes language.
15. `/lkjmcsmp points` shows Cobblestone Points balance.
16. `/lkjmcsmp teleport` opens the teleport menu.
17. `/lkjmcsmp teleport rtp|tpa|tpahere|tp|accept|deny [...]` delegates to the matching teleport command.
18. `/lkjmcsmp team` opens the team menu.
19. `/lkjmcsmp team <subcommand> [...]` delegates to `/team <subcommand> [...]`.

## Completion Rules

1. Root completion includes only subcommands the sender can use.
2. Topic completion is prefix-filtered and deterministic.
3. Player-only completions return empty for console senders.
4. Shop item completions exclude Home slot upgrade keys.
5. Home completions include the sender's normalized Home names.
6. Language completions are loaded from `lang/languages.yml`.
7. Warp completions include configured Warp names.
8. Team and teleport completions expose their command-equivalent action names.

## Failure Rules

1. Console senders can read help but cannot run player-only actions.
2. Missing permissions include the permission node.
3. Unknown subcommands show root help and return success to avoid Bukkit usage spam.
