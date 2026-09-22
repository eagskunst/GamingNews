# GamingNews

Android app that aggregates video game news from RSS feeds and upcoming game releases from IGDB.

<a href='https://play.google.com/store/apps/details?id=com.eagskunst.emmanuel.gamingnews&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80'/></a>

## Features

- News feed aggregated from English and Spanish gaming sites, grouped by platform (All, PlayStation, Xbox, Nintendo, PC)
- Reviews tab with localized Eurogamer feeds
- Upcoming game releases powered by the IGDB API
- Save articles, search, and in-app reader mode
- Muted words to hide articles by title keywords
- Configurable feed sources, notification topics, and a daily reminder
- Light/dark theme and adaptive navigation (bottom bar or rail)

## Tech stack

- Kotlin, Jetpack Compose (Material 3), single-Activity with Navigation Compose
- Clean Architecture (domain/data layers), Hilt DI, UDF ViewModels
- Room, DataStore, WorkManager
- Retrofit + kotlinx.serialization (IGDB v4 via Twitch OAuth), RSS-Parser
- Coil, jsoup/readability4j
- Firebase (Analytics, Cloud Messaging, Realtime Database)

## Building

Requirements: Android Studio (or JDK 17) and the Android SDK.

1. Clone the repository.
2. Add your Firebase configuration file at `app/google-services.json` (from the Firebase console).
3. Add Twitch credentials for the IGDB API to `local.properties`:

   ```properties
   twitch.client.id=YOUR_TWITCH_CLIENT_ID
   twitch.client.secret=YOUR_TWITCH_CLIENT_SECRET
   ```

4. Build the debug APK:

   ```bash
   ./gradlew :app:assembleDebug
   ```

Release builds additionally require a `keystore.properties` file with `storeFile`,
`storePassword`, `keyAlias`, and `keyPassword`.

## Testing

```bash
./gradlew :app:testDebugUnitTest          # unit + Robolectric/Compose tests (JVM)
./gradlew :app:connectedDebugAndroidTest  # instrumented Room tests (device/emulator)
```

See [docs/testing.md](docs/testing.md) for the full testing strategy.

## Agent skills

Shared agent skills live in [`.agents/skills/`](.agents/skills/). `.claude/skills` and
`.devin/skills` are symlinks to that folder, so any harness reads the same set of skills.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## Contributing

Found a bug or want to add a feature? Open an issue or a pull request.
