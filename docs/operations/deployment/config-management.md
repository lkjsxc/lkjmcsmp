# Configuration Management

## Config Owners

- `config.yml`: gameplay defaults, cooldowns, limits, menu toggles, proxy-aware teleport behavior
- `shop.yml`: base exchange rates
- `milestones.yml`: pseudo-advancement definitions

## Rules

1. Base rates are immutable docs contract values unless seasonal override is applied through command.
2. Seasonal overrides persist in SQLite runtime tables.
3. Config reload command must validate schema before apply.
4. Invalid config reload leaves previous config active.

## Teleport Config Keys

- `teleport.request-timeout-seconds`
- `teleport.rtp-cooldown-seconds`
- `teleport.rtp-min-radius` (default `1000`)
- `teleport.rtp-max-radius` (default `100000`)
- `teleport.rtp-attempts`
- `teleport.rtp-world-whitelist`
- `teleport.first-join.enabled`
- `teleport.first-join.world`

## Menu Config Keys

- `menus.show-picker-refresh-controls` (default `true`)

## Menu Refresh Contract

1. Primary refresh path is event-driven after state-changing actions.
2. Background auto-refresh reopen loops are disallowed.
3. Picker menus provide explicit manual refresh controls.

## Proxy Runtime Validation

1. Startup reads proxy-related server configs and emits explicit diagnostics.
2. Velocity-enabled mode with missing secret is configuration-invalid.
3. Proxy mismatch warnings must be actionable and include file/key context.
