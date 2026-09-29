# Engineering Roadmap

## Product Direction

Keep `lkjmcsmp` a survival-focused, GUI-first Minecraft plugin. Player progress must remain understandable and recoverable when an operation fails. Keep Java and the existing domain/persistence/platform separation; a language rewrite does not resolve transaction or scheduler boundaries.

Server provisioning, proxy fleets, and cross-server identity are separate infrastructure concerns. Do not grow a second control plane inside this plugin. Preserve existing worlds, balances, and settings unless a separately reviewed migration explicitly changes them.

## Decision Order

1. Protect earned assets and return locations.
2. Make failures visible and recovery repeatable.
3. Prove behavior on the exact supported server build.
4. Remove unnecessary blocking and stale UI actions.
5. Add gameplay depth and polish after those guarantees hold.

These priorities are revisable in light of measurements and player feedback. Existing undocumented behavior is not an architectural constraint, but persisted player assets are not disposable test data.

## Completed Foundation: Atomic Home Purchases

The Home upgrade, Points debit, and ledger entry now share one SQLite transaction. The fixed catalog is checked by the transaction owner, and an expected-slot comparison rejects stale purchases. There is no public uncharged slot-grant method or compensating-refund path for this database-only operation.

Both rollback-journal and WAL tests cover all 21 upgrades, insufficient funds, stale requests, five injected failure stages, and concurrent writers. This does not make two separate explicit purchases one idempotent request.

## Next: Durable External Effects

`PointsService` still coordinates inventory and asynchronous service effects outside SQLite. Its cobblestone conversion removes inventory before crediting Points, and service-refund errors can be swallowed. The Home transaction fix does not repair those paths.

Before extending the shop or temporary End:
- Record a purchase identity, debit, effect state, and recovery decision durably.
- Distinguish a confirmed failure from an unknown effect outcome. Do not blindly retry item grants or refunds after an ambiguous crash.
- Make duplicate callbacks and repeated recovery requests harmless; surface unresolved operations to administrators.
- Test disconnect, entity-scheduler retirement, restart, effect failure, and refund failure at each boundary.

SQLite cannot atomically commit Minecraft inventory or world effects. A purchase journal must specify which outcomes are safely retryable, which need reconciliation, and which require intervention. Do not claim exactly-once external delivery merely because a receipt exists.

## Platform and Concurrency Boundary

Continue to use entity/region/global schedulers according to ownership. Long-term, capture immutable player intent on the owning scheduler, execute bounded database work away from tick threads, and apply results on the current entity scheduler. Recheck conditions that may change while awaiting completion.

Measure database wait and action latency before choosing queues or caches. Define bounded admission, per-player ordering, cancellation, and shutdown behavior before adding asynchronous execution. Never move Bukkit object access into a generic database executor.

For temporary dimensions, explicitly verify world creation, region-owned block changes, transfer, evacuation, unload, and deletion on each intended server version. Persist return information before movement, and keep recovery records until return is confirmed. Unsupported operations must fail without charging or deleting player state.

## Verification and Release Gates

Separate four proof levels: pure/domain tests, real SQLite transactions, plugin boot/console dispatch, and real-player multi-region scenarios. Source-text markers are only structural checks; they do not certify gameplay.

The September 2026 checkpoint verifies Java 21 and Folia 1.21.11 build 14 for startup and console smoke. It is not a production certification or a Folia 26.1.2 compatibility claim. Add a versioned acceptance matrix and continuous repository checks before broadening compatibility claims.

Keep test servers isolated. The existing base Compose file still publishes ports and uses development credentials; it is not a hardened public deployment. The checkpoint smoke removed all published ports through an override. A reusable isolated smoke configuration and secure deployment defaults remain follow-up work.

Require a backup/restore rehearsal, explicit database compatibility review, and player-facing acceptance before a production rollout. Do not bundle a build verification with an automatic world or plugin update.

## Player Experience After Reliability

Keep one consistent Home/purchase flow across commands and menus, show pending or failed actions honestly, and avoid surprise repeated purchases. Retain English/Japanese behavior while moving remaining hard-coded feedback into the message layer in a separate tested batch.

Judge new systems by whether they create useful survival choices and cooperation, not by the number of menus or services. Preserve small reviewable changes with explicit acceptance evidence.

## References

- [Home transaction contract](../architecture/data/consistency.md)
- [SQLite transaction behavior](https://www.sqlite.org/lang_transaction.html)
- [Paper/Folia scheduler ownership](https://docs.papermc.io/paper/dev/folia-support/)
