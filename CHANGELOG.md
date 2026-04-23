# Changelog

## 0.1.0

### Added
- Self-profile command (`/profile`) and menu showing points, team, achievements, and playtime.
- Respawn random teleport: players respawning at initial spawn are automatically RTP'd.
- `PlayerHead` factory in `MenuItems` for correct skin rendering.
- Team member list display in team menu.
- Temporary dimension death handling: respawn inside temp dimensions redirects to origin.
- Temporary dimension portal blocking: `PlayerPortalEvent` is cancelled inside temp dimensions.
- `findParticipantReturn` DAO method for active instance lookups.

### Changed
- Documentation overhaul: added `docs/product/features/` directory, updated all contracts.
- `ShopEffectExecutor` interface now receives `ShopEntry` for environment-aware execution.
- Temporary dimension world creation now runs on the global scheduler for Folia safety.
- Root menu now includes Profile button; Close Menu shifted to slot 25.
- Config key corrected from `temporary-end` to `temporary-dimension`.

### Fixed
- Cobblestone convert button no longer requires empty cursor.
- Nether Star menu token is removed from death drops.
- Player heads in picker and team menus render correct skins instead of Steve.
- Temporary dimension creation now respects the shop entry's `environment` value.
