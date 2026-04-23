# Local Server Deployment

## Goal

Run a local Folia server with `lkjmcsmp` installed for manual validation.

## Steps

1. Build plugin jar through compose `verify` or `./gradlew --no-daemon shadowJar`.
2. Start compose `folia` service.
3. Join server and run `/menu`.
4. Validate core command set.

## Rules

1. Server image and startup args are managed via compose files.
2. Plugin jar path mounted into `plugins/` is deterministic.
3. EULA acceptance for local test server is controlled by compose env.
