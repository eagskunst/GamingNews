# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Muted words: local title-only filtering with user-managed mute rules supporting Contains, Whole word, and Exact phrase match modes, per-rule case sensitivity, and scopes limited to Everywhere or selected news tabs.
- "Muted words" management screen in Settings with a rule-count summary, add/edit bottom sheet (match mode, case sensitivity, tab scope), delete confirmation, validation errors, and an "Apply global mute rules to Reviews" opt-in switch.
- Inline feed notice in News and Reviews showing the hidden-article count with Show hidden / Hide again and Manage actions; revealed muted articles keep their chronological order and display a "Muted" badge, and the notice collapses while scrolling down and reappears when scrolling up.
- Distinct all-muted empty state with reveal/manage actions when every article in a tab or search result is muted.
- Room persistence for mute rules and selected-tab associations with transactional CRUD, in-transaction duplicate protection, and a non-destructive 2 → 3 migration preserving saved articles and releases.
- Comprehensive coverage: matcher/validator, reactive use-case filtering, ViewModels, Compose screens, DataStore preference, and instrumented Room DAO and migration tests.

### Changed
- Search and mute filtering moved from Compose into the domain use cases so rendered results and muted counts share the same candidate set; rule edits, search changes, and reveal toggles re-filter the cached snapshot without restarting network requests.
- The "new articles" banner count is computed only on the first emission of a refresh so filtering changes can no longer re-trigger it.
- Hardened Twitch token caching with an expiration safety margin, client-ID association, and release-only credential validation.
- Disabled release HTTP logging and limited debug IGDB diagnostics while excluding Twitch authentication traffic.

### Fixed
- Recovered from rejected IGDB tokens with one coordinated renewal and one bounded retry, including concurrent release requests.
- Preserved cached releases and displayed localized errors when refresh or pagination fails.
- Prevented overlapping release refresh and pagination operations.

## [2.7.0] - 2026-09-11

### Added
- Added Reviews as a fourth adaptive navigation destination with search, pull-to-refresh, retry states, bookmark synchronization, and existing article open/share actions.
- Added localized Eurogamer review feeds selected by device language: Eurogamer ES for Spanish locales and Eurogamer EN for English and unsupported locales.
- Added featured, compact-thumbnail, and text-only review cards that respect the existing image-loading preference.
- Added review feed caching, stale-data fallback, URL deduplication, conservative date handling, and comprehensive repository, ViewModel, Compose, catalog, mapper, and Room migration coverage.
- Added nullable article authors across RSS mapping, Room persistence, saved article reloads, and shared article metadata.

### Changed
- Injected the shared RSS parser into the RSS remote data source for improved testability.
- Bumped the Room database to version 2 with a migration that preserves existing saved articles and releases.

### Fixed
- Prevented the initial Reviews load from displaying both pull-to-refresh and centered loading indicators.
- Prevented failed review refreshes from replacing a valid fresh cache entry or displaying an unknown publication date as decades old.
- Surfaced bookmark persistence failures in the Reviews screen state.

## [2.6.0]

### Added
- Feed provider selection: users can now enable or disable individual RSS providers from a new "Customize Feed" screen in Settings, accessible via the "Feed & Sources" section.
- Providers are grouped by category (All, Playstation, Xbox, Nintendo, PC) with filter chips; each provider row shows a name and checkbox.
- Disabled providers are excluded from all news fetches (initial load, pull-to-refresh, and category switches), reducing bandwidth and battery use.
- "Restore defaults" action in the top bar re-enables all providers.
- Provider selections are persisted via DataStore and survive app restarts.
- Enriched `urls.json` with stable provider IDs and human-readable names.
- In-Settings NavHost navigation between the settings root and the feed sources screen.
- Provider ID stability snapshot test to guard against accidental renames or removals that would break users' saved preferences.
- Comprehensive test coverage: DataStore persistence, repository, use cases, ViewModel, and updated existing NewsViewModel/NewsScreen/SettingsScreen tests.

## [2.4.0] - 2026-08-22

### Added
- Reselecting an already-active bottom navigation / rail tab now scrolls that tab's list back to the top.
- A short-lived bubble appears at the top of the news feed after a pull-to-refresh when new articles are available, themed with the current Material color scheme and auto-dismissing after 3 seconds.

### Fixed
- Overextended article titles in `ArticleCard` now use `TextOverflow.Ellipsis` on the second line instead of being clipped.

## 2.2.0

### Added
- Dynamic theme support

### Fixed
- Pull-to-refresh was not refreshing the feed. Now it works as expected

## [Released]

## [2.1.0] - 2026-08-13

### Added
- Pull-to-refresh on the news feed screen.

### Fixed
- Fixed a large gap between the status bar and the toolbar caused by the outer `Scaffold` and each screen's `Scaffold` both reserving space for the status bar inset.
- Fixed the status bar/navigation bar icon contrast not following the app's selected theme (it now updates reactively instead of relying on the system dark mode setting).

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
