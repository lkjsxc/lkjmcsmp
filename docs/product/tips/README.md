# Tips

## Goal

Expose command discovery through low-noise multilingual tips so players can learn command and menu routes during normal play.

## Rules

1. Tips are enabled by default and sent about once per hour.
2. The default interval is `72000` ticks.
3. Tips are chat messages, not action-bar HUD overlays.
4. Tips use each player's selected language at send time.
5. If a player's language lacks a tip, English is the fallback.
6. The bundled catalog contains about 200 tips.
7. Tips focus on command routes, menu routes, safety behavior, and feature discovery.
8. Tip rotation is deterministic enough to avoid showing the same tip repeatedly to the same player.
9. Tips must never require operator-only commands unless the tip text names the permission expectation.
10. Disabling tips in config stops scheduling and sending.

## Child Index

- [delivery.md](delivery.md): scheduling and player delivery rules
- [catalog-structure.md](catalog-structure.md): resource file layout and localization contract
