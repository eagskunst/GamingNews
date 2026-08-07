# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Migrated the entire application source from Java to Kotlin.
- Upgraded Android Gradle Plugin from 3.5.3 to 9.3.0.
- Upgraded Gradle wrapper from 5.4.1 to 9.5.0.
- Raised `compileSdk` and `targetSdk` to 36 and `minSdk` to 23.
- Switched Dagger annotation processing from `kapt`/`annotationProcessor` to KSP.
- Bumped all dependencies to current stable versions, including AndroidX, Firebase, OkHttp, Retrofit, Gson, and Picasso.
- Replaced legacy RSS parser `com.prof.rssparser` with `com.prof18.rssparser:rssparser:5.0.3`.
- Replaced `jcenter()` with `mavenCentral()` and `google()`.
- Made release signing configuration optional based on the presence of `keystore.properties`.

### Removed
- Removed AdMob banner integration (ads dependency, `AdView`, and manifest metadata removed; restore instructions left in code comments).
- Removed legacy `okhttp-urlconnection` and `legacy-support-v4` dependencies.
- Removed MultiDex since `minSdk` is now 23.

### Fixed
- Fixed Android 12+ manifest `exported` attributes.
- Fixed `PendingIntent` mutability flags for Android 12+.
- Added `POST_NOTIFICATIONS` permission for Android 13+.
- Fixed non-final resource ID `switch` usage for AGP 8+.
- Fixed `RecyclerView` layout height issue in the releases fragment.
- Fixed `NewsListFragment` and `ReleasesFragment` context/activity null-safety.

### Added
- Added Kotlin source files for all models, utility classes, fragments, activities, adapters, API, DI, and tests.
- Added `pluginManagement` block in `settings.gradle`.

### Notes
- `org.gradle.java.home` in `gradle.properties` points to a local JDK 17 path and may need adjustment on other machines or CI.
- `Credentials.java` remains gitignored; the last Java file (`credentials/Credentials.java`) should be converted to Kotlin when its secret values are finalized.
