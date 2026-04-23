# Temporary End Purchase and Creation

## Cost and Trigger

1. Consuming `10,000` Maruishi Points creates one temporary End instance.
2. The cost is configurable under `temporary-end.cost`.
3. Purchase validates balance atomically before deducting Maruishi Points.
4. Reason code for ledger: `SHOP_PURCHASE` (temporary end passes are purchased through the shop system).
5. On failure, Maruishi Points are not deducted and the player receives an explicit message.

## Purchase Paths

1. **Shop path (primary)**: Open Points Shop, select `Temporary End Pass`, and click Purchase. This deducts Maruishi Points and triggers instance creation automatically.
2. **Command path (secondary)**: `/tempend purchase` calls `PointsService.purchase("temporary_end_pass", 1)` then `TemporaryEndManager.createInstance`.
3. All paths share the same balance validation, ledger reason code, and instance creation logic.

## Instance Creation

1. Creation queues on the global region scheduler for Folia safety.
2. Each instance receives a unique world name: `lkjmcsmp_tempend_<uuid>`.
3. World generation uses `Environment.THE_END` for vanilla End terrain, dragon, cities, and Elytra.
4. If world creation fails after Maruishi Points are deducted, the purchase is refunded immediately with reason `TEMPORARY_END_REFUND` and logged.
5. Each player may have at most one active instance at a time; a second purchase is rejected with the remaining time of the active instance.

## Player Transfer

1. At activation, all players within `10` blocks of the activation origin are captured.
2. Captured players are teleported to the obsidian platform at `(100, 49, 0)` in the new End world.
3. The activation origin is recorded as the return location for all participants.
4. Invalid-state players (offline, dead, vanished) are skipped.
5. The transfer radius is configurable under `temporary-end.transfer-radius`.

## Access Policy

1. The instance is open: any player may enter the world while it exists.
2. Only initially captured participants are guaranteed evacuation on expiry.
3. Late entrants are not tracked for automatic return.
