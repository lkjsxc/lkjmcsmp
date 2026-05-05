# Build Hygiene

## Goal

Keep local and CI builds deterministic and free from environment-specific failures.

## Gradle Wrapper

1. The repository must include `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.properties`.
2. Wrapper version must match the CI image (`8.10.2`).
3. Local builds use `./gradlew`; CI builds use the preinstalled Gradle.

## Daemon Policy

1. Verification scripts (`scripts/verify.sh`) must use `--no-daemon` to prevent file-lock races.
2. Local development may use the daemon, but `clean` tasks should guard against stale locks.
3. Docker compose verification may leave ignored build artifacts owned by the container user.
4. Host-side Gradle failures caused only by ignored build artifact ownership do not override a passing compose gate.

## Docker Mount Hygiene

1. The `build/libs` directory is mounted read-only into the Folia container.
2. After smoke tests, run `docker compose down -v` to release volume locks before local `gradle clean`.

## Line Limit Gates

1. `scripts/check_lines.py` enforces docs `<= 300` lines and source `<= 200` lines.
2. New exclusions require an update to `line-limits.md`.
