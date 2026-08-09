# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0-beta04] - 2026-08-09

### Changed
- Updated `com.prof18.rssparser:rssparser` from `5.0.3` to `6.1.8`, migrating to the new `RssParser`/`RssChannel`/`RssItem` API (`Channel.articles` renamed to `RssChannel.items`).
- Added a custom Coil `ImageLoader` that sends a browser-like `User-Agent` header on image requests, fixing HTTP 403 errors from some feed image CDNs.

### Fixed
- `ArticleCard` no longer shows a broken image placeholder: the image is hidden if it fails to load or if the "Load images" setting is disabled.

## [2.0.0-beta03] - 2026-08-09

### Fixed
- Fixed dark theme not applying by wiring the `UserPreferences.darkTheme` setting into `GamingNewsTheme`.
- Fixed dark theme colors being too pale/washed out; card backgrounds now use a distinct `surfaceVariant` color instead of blending into the page background.
- Fixed releases pagination: releases are no longer filtered down to the current month only, and the release list now paginates through the current year (or up to 8 months ahead) using IGDB's offset/limit.
- Fixed news feeds being re-fetched every time a category tab was reselected by adding an in-memory cache to `DefaultNewsRepository`.

### Changed
- Raised `minSdk` to 24 and `targetSdk` to 37.
- Temporarily hidden the "Notification topics" section in Settings.

## [2.0.0-beta02] - 2026-08-09

### Changed
- Refreshed the RSS feed sources in `urls.json` with a new, updated set of URLs per language/publisher.
- Renamed news category tabs from console names to platform brand names: PS4 → Playstation, Switch → Nintendo (All, Xbox, and PC labels unchanged).
- Renamed `NewsCategory` enum values and related `FeedUrlsCategoryDto` fields from console-specific names to platform names (`PS4`/`ps4Urls` → `SONY`/`sonyUrls`, `XBOX`/`xboxUrls` → `MICROSOFT`/`microsoftUrls`, `SWITCH`/`switchUrls` → `NINTENDO`/`nintendoUrls`) and updated the corresponding JSON keys (`ps4_urls` → `sony`, `xboxO_urls` → `microsoft`, `switch_urls` → `nintendo`).

### Removed
- Removed pre-migration legacy MVP/Dagger code: leftover fragments, adapters, legacy models, old utility helpers (`NotificationMaker`, `SharedPreferencesLoader`, etc.), `ArticlesFromNotificationActivity`, `SaveArticleReceiver`, and the now-unused `Credentials.java`.
- Removed associated legacy resources: layouts, menus, animations, preferences XML, legacy PNG icons, unused drawable XMLs, dimens, and unused strings/colors.
- Pruned dependencies only used by the deleted legacy code: `gson`, `picasso`, `retrofit-converter-gson`, `constraintlayout`, `cardview`, `recyclerview`, `swiperefreshlayout`, `navigation-fragment`, and `navigation-ui`.

## [2.0.0-beta01] - 2026-08-07

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
