# GamingNews — Agent Notes

## Project Overview
GamingNews is an Android app that aggregates RSS gaming news and upcoming game releases from IGDB. It is being modernized to a single-Activity, Compose + Hilt + Clean Architecture codebase.

## Build Commands

```bash
# Debug build
./gradlew :app:assembleDebug

# Run unit tests (includes Robolectric-based DataStore/Compose tests)
./gradlew :app:testDebugUnitTest

# Run instrumented tests (Room DAO tests; requires a connected device/emulator)
./gradlew :app:connectedDebugAndroidTest

# Clean build
./gradlew clean :app:assembleDebug
```

See `docs/testing.md` for the full testing strategy (frameworks, folder layout, conventions,
known limitations).

## Architecture

- **UI layer**: Jetpack Compose screens under `ui/...` with UDF ViewModels.
- **Domain layer**: Repository interfaces, use cases, and domain models under `core/domain/`.
- **Data layer**: Repository implementations, local Room/DataStore sources, and remote Retrofit/RSS sources under `core/data/`.
- **DI**: Hilt modules under `di/module/`. Legacy Dagger components are still present but unused.

## Important Configuration

- `compileSdk = 37`, `targetSdk = 37`, `minSdk = 24`.
- IGDB v4 requires Twitch OAuth credentials. Add to `local.properties`:

```properties
twitch.client.id=YOUR_TWITCH_CLIENT_ID
twitch.client.secret=YOUR_TWITCH_CLIENT_SECRET
```

These are exposed as `BuildConfig.TWITCH_CLIENT_ID` and `BuildConfig.TWITCH_CLIENT_SECRET`.

## Migration Status

1. Build system migrated to Kotlin DSL + version catalog.
2. Clean Architecture domain + data layers introduced with IGDB v4 OAuth.
3. Hilt dependency injection wired across layers.
4. Main screens rewritten in Compose with Navigation Component and adaptive navigation.
5. Edge-to-edge rendering enabled.
6. Daily reminder WorkManager scheduled from settings.
7. Unit test coverage added across mappers, data sources, repositories, use cases, ViewModels,
   and Compose UI (screens/components), plus instrumented Room DAO tests. See `docs/testing.md`.

## Remaining Cleanup

- Add a lint baseline before release.
- `RssRemoteDataSource` constructs `RssParser()` internally rather than taking it as a
  dependency, which limits how thoroughly it can be unit tested — consider injecting it.
