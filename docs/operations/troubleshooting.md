# Troubleshooting

## Goal

Surface common enable-time and runtime failures with deterministic diagnostics.

## Plugin Fails to Enable

### Database Path Error
- Symptom: `SQLException` during `onEnable`.
- Cause: `database.file` config points to a non-writable path.
- Fix: Ensure the server process can write to the configured database directory.

### Missing Command Registration
- Symptom: `Command missing in plugin.yml` in startup log.
- Cause: A command is registered in code but absent from `plugin.yml`.
- Fix: Add the command and permission to `plugin.yml`, then rebuild.

### HUD Task Duplication on Reload
- Symptom: Duplicate idle refresh messages or rapid action-bar flicker after `/reload`.
- Cause: Old `ActionBarHudService` scheduled tasks were not cancelled before re-enable.
- Fix: Ensure `onDisable` calls `services.hud().stop()` and that `stop()` cancels scheduled tasks.

## Build Failures

### Clean Fails with `Unable to delete directory`
- Symptom: `gradle clean` throws `IOException` on `build/`.
- Cause: Gradle daemon or another process holds files open.
- Fix: Run `gradle --stop` or use `--no-daemon` for all build commands.

### Shadow Plugin Deprecation
- Symptom: Build warns about Gradle 9 incompatibility.
- Cause: Shadow plugin or Gradle feature usage.
- Fix: Update `build.gradle.kts` to latest stable shadow plugin and avoid deprecated APIs.

## Runtime Failures

### Menu Open Fails Silently
- Symptom: Player clicks slot `8` but no menu opens.
- Cause: `MenuService` or `HotbarMenuService` threw an unhandled exception.
- Fix: Check server logs for `NullPointerException` in `MenuService.onClick`.

### Teleport Request Never Arrives
- Symptom: `/tpa` sends no message to target.
- Cause: Target is offline or the request expired immediately.
- Fix: Verify `teleport.request-timeout-seconds` is > 0 and both players are online.
