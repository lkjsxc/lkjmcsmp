# Temporary Dimension Purchase and Creation

## Summary

Purchasing a dimension pass reserves Cobblestone Points, creates a new isolated world of the configured environment type, then reports final success only after creation and transfer scheduling succeed.

## Cost and Trigger

1. Default cost: `10,000` Cobblestone Points.
2. Cost is configurable under `temporary-dimension.cost`.
3. Purchase validates balance before deducting Cobblestone Points.
4. Reason code for ledger: `SHOP_PURCHASE`.
5. On failure, Cobblestone Points are not deducted and the player receives an explicit message.

## Purchase Paths

1. **Shop path (primary)**: Open Points Shop, select the dimension pass item, and click Purchase. This deducts Cobblestone Points and triggers instance creation automatically.
2. **Command path (secondary)**: `/tempdim purchase` runs the same purchase flow using the shop entry's configured environment.
3. All paths share the same balance validation, ledger reason code, and instance creation logic.

## Environment Selection

1. Shop entries specify `environment` in `shop.yml`. Supported values: `THE_END`, `NETHER`, `NORMAL`.
2. Command path uses the same shop entry configuration.
3. The environment is stored in the instance record and determines world generation rules.

## Instance Creation

1. Creation queues on the global region scheduler for Folia safety.
2. Each instance receives a unique world name: `lkjmcsmp_tempdim_<uuid>`.
3. World generation uses the configured `Environment`.
4. If creation fails after Cobblestone Points are deducted, the exact deducted amount is refunded immediately with reason `TEMPORARY_DIMENSION_REFUND` and logged.
5. Each player may have at most one active instance at a time; a second purchase is rejected with the remaining time of the active instance and an exact refund.
6. Final success chat is sent only after world creation, spawn preparation, instance persistence, and transfer scheduling complete.

## Player Transfer

1. At activation, all players within `5` blocks of the activation origin are captured.
2. Captured players are teleported to the environment-specific spawn location:
   - `THE_END`: obsidian platform at `(100, 49, 0)`
   - `NETHER`: safe location near `(0, 70, 0)`
   - `NORMAL`: safe location near `(0, 70, 0)`
3. Each captured player's current location is recorded as their individual return location.
4. Invalid-state players (offline, dead, vanished) are skipped.
5. The transfer radius is configurable under `temporary-dimension.transfer-radius`.
6. **Folia Safety:** player location and validity checks run on each player's own scheduler. The global scheduler only enumerates candidates and schedules per-player validation tasks.

## Access Policy

1. The instance is open: any player may enter the world while it exists.
2. All initially captured participants are tracked for automatic return on expiry.
3. Late entrants are not tracked for automatic return.

## Cross-References

- [shop-integration.md](shop-integration.md): visual placement in the Points Shop
- [lifecycle.md](lifecycle.md): expiry and cleanup after creation
- [access-rules.md](access-rules.md): gameplay inside active instances
