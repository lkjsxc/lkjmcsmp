# Temporary End Purchase and Creation

## Cost and Trigger

1. Consuming `10,000` points creates one temporary End instance.
2. The cost is configurable under `temporary-end.cost`.
3. Purchase validates balance atomically before deducting points.
4. Reason code for ledger: `TEMPORARY_END_PURCHASE`.
5. On failure, points are not deducted and the player receives an explicit message.

## Purchase Paths

1. **Command path**: `/tempend purchase` triggers direct purchase.
2. **Shop path**: Buying the `temporary_end` shop item for `10,000` points triggers the same creation flow.
3. **Menu path**: Root menu -> Temporary End -> Purchase button triggers the same creation flow.
4. All paths share the same balance validation, ledger reason code, and instance creation logic.

## Instance Creation

1. Creation queues on the global region scheduler for Folia safety.
2. Each instance receives a unique world name: `lkjmcsmp_tempend_<uuid>`.
3. World generation uses `Environment.THE_END` for vanilla End terrain, dragon, cities, and Elytra.
4. If world creation fails, the purchase is refunded and logged.

## Player Transfer

1. At activation, all players within `10` blocks of the activation origin are captured.
2. Captured players are teleported to the new End world's spawn platform.
3. The activation origin is recorded as the return location for all participants.
4. Invalid-state players (offline, dead, vanished) are skipped.
5. The transfer radius is configurable under `temporary-end.transfer-radius`.

## Access Policy

1. The instance is open: any player may enter the world while it exists.
2. Only initially captured participants are guaranteed evacuation on expiry.
3. Late entrants are not tracked for automatic return.
