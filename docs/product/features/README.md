# Cross-Cutting Feature Contracts

## Goal

Define player-facing behaviors that span multiple menus, commands, or systems.

## Rules

1. Each feature has one canonical owner file.
2. GUI and command contracts reference these files rather than duplicating rules.
3. Changes to a cross-cutting feature update this section first.

## Child Index

- [death-drops.md](death-drops.md): hotbar menu token drop prevention
- [respawn-rtp.md](respawn-rtp.md): random teleport on initial-spawn respawn
- [initial-trigger-rtp.md](initial-trigger-rtp.md): countdown random teleport from the initial spawn zone
- [profile.md](profile.md): self-profile command and menu
- [player-heads.md](player-heads.md): correct skin rendering for PLAYER_HEAD items
- [stair-sitting.md](stair-sitting.md): empty-hand stair sitting and cleanup
