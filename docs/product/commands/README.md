# Command Contracts

Use this section for exact command names, aliases, required permissions, and error outcomes.

## Rules

1. Every GUI action must have a command-parity path.
2. Every command returns explicit success/failure feedback.
3. Cooldown-denied actions must report remaining time.
4. Permission-denied actions must identify the missing node.

## Child Index

- [teleport.md](teleport.md): `tp`, `rtp`, `tpa`, and request flow
- [homes-warps.md](homes-warps.md): home and warp command behavior
- [party.md](party.md): lightweight party operations
- [utility.md](utility.md): menu and economy utility commands
