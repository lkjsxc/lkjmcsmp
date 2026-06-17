# HUD Contracts

## Summary

Player-facing heads-up display is delivered exclusively through the action bar for players with the HUD preference enabled. These contracts define priority arbitration, overlay behavior, and the enabled-player idle guarantee.

## Rules

1. Action bar is the only runtime HUD channel for persistent and ephemeral gameplay status.
2. HUD updates are driven by a per-player periodic re-evaluation task; no global idle ticker.
3. Priority is deterministic across all overlapping systems.
4. Overlays are temporary and expire logically via TTL; expiry is evaluated during periodic ticks.
5. Messages are scoped by `source`, carry an explicit priority, and may include a TTL.
6. The renderer sends the visible action-bar text every `10` ticks by default so it remains visible without excessive packet traffic.
7. Idle text must never be blank for an online player who has the HUD enabled.
8. When a player disables the HUD, this plugin stops sending action-bar packets and does not clear the channel.

## Child Index

- [priority-arbitration.md](priority-arbitration.md): priority enum, source identifiers, and tie-breaking
- [idle-guarantee.md](idle-guarantee.md): always-on idle content, force-send, and reclaim rules
- [overlay-sources.md](overlay-sources.md): teleport, combat, gameplay, and dimension overlay contracts
- [renderer-contract.md](renderer-contract.md): per-player periodic task, dedup, and packet dispatch
