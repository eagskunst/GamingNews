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
8. Game release reminders: users can follow exact-date releases from the Releases tab
   (notification bell + reminder-offset sheet). Follows persist in Room (`followed_games`,
   DB version 6), a daily 09:00 WorkManager check posts localized reminders and silently
   re-syncs changed release dates. Only *active* follows (unreleased, or released at most
   `ACTIVE_FOLLOW_GRACE_DAYS` = 7 days ago) take part: the IGDB fetch is bounded below by
   that horizon and each game keeps the release row closest to its stored day, so
   historical per-platform launches can't corrupt follows. Older follows are never pruned,
   fetched, reconciled or notified — they stay in Room until the user removes them — and
   the check is cancelled once no active follow remains. All notification posting goes
   through `AppNotifier`/`ReleaseNotifier`.

9. Game detail page (issue #8): tapping a Releases card or a release notification opens
   `ui/gamedetail` (About / Media / Related tabs) for the IGDB game ID. Details come from one
   `games` query and are cached as JSON in Room (`game_details`, DB version 7, refreshed
   after 24 h). Follows reuse `GameRelease` via `GameDetails.pendingRelease`, so the Follow
   button only shows while a tracked platform still has an exact upcoming date. New DB
   versions need their schema JSON copied into `app/src/androidTest/assets/` for the
   migration tests.

10. Games tab (issue #26): the Releases destination is labelled "Games" and
    `ui/releases/ReleasesScreen` has two tap-only top tabs (`rememberSaveable`): Releases
    (default, unchanged) and Followed, which lists every `followed_games` row
    (`FollowedGamesRepository.observeAll()`, ordered by `sortedForFollowedList`) with
    unfollow/remove + Undo and no search, filters or refresh.

## Agent Skills

All skills live in `.agents/skills/` (the cross-harness standard location). `.claude/skills`
and `.devin/skills` are directory symlinks to `../.agents/skills` — add new skills under
`.agents/skills/` only, and every harness picks them up automatically. Installed skills are
tracked in `skills-lock.json`.

## Remaining Cleanup

- Add a lint baseline before release.
