# Root Layout Contract

## Required Paths

- `docs/`
- `src/main/java/`
- `src/main/resources/`
- `src/test/java/`
- `scripts/`
- `docker-compose.yml`
- `docker-compose.verify.yml`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`

## Rules

1. Product and architecture contracts live only under `docs/`.
2. Build and verification scripts live under `scripts/`.
3. Runtime config defaults live under `src/main/resources/`.
