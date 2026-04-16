# Proxy Runtime Contract

## Goal

Keep teleport/session behavior predictable when Paper/Folia runs behind a proxy such as Velocity.

## Scope

- Intra-server behavior only (no cross-server teleport handoff).
- Startup validation and diagnostics for proxy settings that affect identity/session assumptions.

## Validation Rules

1. Read and validate:
   - `config/paper-global.yml` (`proxies.velocity.*`, `proxies.bungee-cord.*`)
   - `spigot.yml` (`settings.bungeecord`)
2. If Velocity is enabled, secret must be non-empty.
3. Proxy-mode conflicts produce explicit startup warnings with file/key references.
4. Validation does not silently mutate server config.

## Operational Rules

1. `/tp` and `/rtp` remain local-server operations.
2. First-join RTP identity scope is player UUID.
3. Invalid proxy config must be visible in logs before player command traffic.
