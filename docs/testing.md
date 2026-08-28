# Testing Strategy

This document describes how GamingNews is tested: frameworks used, where tests live, how to run
each kind, and the conventions to follow when adding new tests.

## Frameworks

- **JUnit4** — test runner for all unit and instrumented tests.
- **kotlinx-coroutines-test** — `runTest`, `TestDispatcher`s for deterministic coroutine tests.
- **Turbine** (`app.cash.turbine`) — asserting `Flow`/`StateFlow` emissions.
- **MockK** — mocking, used only where a fake is impractical (Retrofit API interfaces, OkHttp
  classes, third-party wrapped classes). Fakes backed by `MutableStateFlow` are preferred
  everywhere else — see `ArticleOpenModeUseCaseTest.kt` for the canonical style.
- **Robolectric** — runs Android-framework-dependent code (DataStore, `android.text.Html`,
  Jetpack Compose UI) on the JVM as part of `testDebugUnitTest`, so no emulator is required for
  the vast majority of tests. Pinned to API 34 via `app/src/test/resources/robolectric.properties`
  (the stable Robolectric release used here doesn't yet support `compileSdk = 37`).
- **Compose UI Test** (`androidx.compose.ui:ui-test-junit4`) — used together with Robolectric for
  screen/component behavior tests, and with a real device/emulator for `androidTest`.
- **Room testing** (`androidx.room:room-testing`) — in-memory database for DAO instrumentation
  tests.
- **WorkManager testing** (`androidx.work:work-testing`) — `TestListenableWorkerBuilder`,
  `WorkManagerTestInitHelper`.

## Where tests live

```
app/src/test/java/com/eagskunst/emmanuel/gamingnews/
├── testutil/                     Shared test helpers (see below)
├── core/data/mapper/             Mapper unit tests
├── core/data/source/local/       DataStore-backed local source tests (Robolectric)
├── core/data/source/remote/      Remote data source tests (MockK)
├── core/data/repository/         Repository tests (fakes + MockK)
├── core/domain/usecase/          Use case tests (fakes)
├── ui/<feature>/                 ViewModel tests + Robolectric Compose screen tests
├── ui/components/                Robolectric Compose component tests
└── worker/                       WorkManager CoroutineWorker test (Robolectric)

app/src/androidTest/java/com/eagskunst/emmanuel/gamingnews/
└── core/data/source/local/       Room DAO tests against a real in-memory SQLite database
```

## Shared test utilities (`testutil/`)

- `MainDispatcherRule` — swaps `Dispatchers.Main` for a `TestDispatcher`; apply to every
  ViewModel test (`@get:Rule val mainDispatcherRule = MainDispatcherRule()`).
- `TestDispatcherProvider` — implements `core.common.DispatcherProvider` for classes that take a
  `DispatcherProvider` instead of using `Dispatchers.Main` directly.
- `Fixtures` — factory functions for domain models (`Fixtures.newsArticle()`,
  `Fixtures.gameRelease()`, `Fixtures.topic()`, `Fixtures.userPreferences()`) to avoid repeating
  literals across test classes.
- `testutil/fakes/` — one canonical, reusable fake per repository/DAO, shared across every use
  case/ViewModel/Compose UI test that needs it (`FakeNewsRepository`, `FakeReleasesRepository`,
  `FakeTopicsRepository`, `FakeUserPreferencesRepository`, `FakeArticleDao`, `FakeReleaseDao`).
  **Always reuse these instead of writing a new private fake inside a test file** — this keeps
  fake behavior consistent and easy to evolve in one place as the interfaces change.

## Conventions

- **Test names**: BDD-style backtick names, `` `given X when Y then Z` `` or `` `when X then Y` ``.
- **Fakes over mocks**: prefer a small in-memory fake implementing the relevant repository/DAO
  interface (backed by `MutableStateFlow`) over mocking. Use MockK only for coordinator classes
  or third-party/Android-framework types that are impractical to fake (Retrofit APIs, OkHttp,
  Room DAOs are the exception — they're simple interfaces, so fake them instead).
- **No `any()` for asserted values**: when verifying a MockK call, assert exact arguments (or
  capture with `slot`) rather than `any()`, unless the argument's exact content genuinely isn't
  worth asserting.
- Screens take their ViewModel as a plain constructor parameter (not `hiltViewModel()` inside the
  composable), so UI tests construct real ViewModels wired to fake repositories rather than
  faking the whole screen's state.
- Compose UI tests use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` (the v2
  API), not the deprecated `androidx.compose.ui.test.junit4.createAndroidComposeRule`. The v2
  rule uses a `StandardTestDispatcher` for composition (queued execution) instead of
  `UnconfinedTestDispatcher`; call `composeTestRule.waitForIdle()` after an action that triggers
  recomposition (e.g. a click that changes state) before asserting on the result.

## Running tests

```bash
# All JVM unit tests (mappers, repositories, use cases, ViewModels, Robolectric Compose/DataStore tests)
./gradlew :app:testDebugUnitTest

# Instrumented tests (Room DAO tests) — requires a connected device/emulator
./gradlew :app:connectedDebugAndroidTest

# Everything
./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest
```

## Known limitations

- `RssRemoteDataSource` instantiates `RssParser()` internally, so it can't be faked without a
  production refactor; its test only checks that a bad URL fails fast rather than hanging.
- `GetFeedUrlsUseCase` (reads `assets/urls.json` via `Context`) and `OpenArticleUseCase`
  (Android `Intent`/Custom Tabs heavy) are not unit tested directly; they're exercised indirectly
  through the ViewModel tests that mock them.
- `SettingsViewModel.toggleDailyReminder(true)` schedules work via
  `DailyReminderScheduler`/`WorkManager`, which isn't available in a plain (non-Robolectric) JVM
  test — that interaction is covered by `SettingsScreenTest` (Robolectric, with
  `WorkManagerTestInitHelper`) instead.
- The notification-topics section of `SettingsScreen` (gated behind a `false` feature flag in
  source) is not covered by Compose UI tests.
