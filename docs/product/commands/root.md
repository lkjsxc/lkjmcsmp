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
6. `/lkjmcsmp achievement` opens the achievement menu.
7. `/lkjmcsmp achievement claim <key>` matches `/achievement claim <key>`.
8. `/lkjmcsmp profile` opens the profile menu.
9. `/lkjmcsmp settings` opens settings; `/lkjmcsmp language <code>` changes language.
10. `/lkjmcsmp points` shows Cobblestone Points balance.
11. `/lkjmcsmp teleport` opens the teleport menu.
12. `/lkjmcsmp team` opens the team menu; command-mutating team actions stay on `/team`.

## Completion Rules

1. Root completion includes only subcommands the sender can use.
2. Topic completion is prefix-filtered and deterministic.
3. Player-only completions return empty for console senders.
4. Shop item completions exclude Home slot upgrade keys.
5. Home completions include the sender's normalized Home names.
6. Language completions are loaded from `lang/languages.yml`.

## Failure Rules

1. Console senders can read help but cannot run player-only actions.
2. Missing permissions include the permission node.
3. Unknown subcommands show root help and return success to avoid Bukkit usage spam.
