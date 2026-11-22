# Cinevo

Cinevo is an Android app for browsing movies from [The Movie Database (TMDB)](https://www.themoviedb.org/). Pick a
genre, scroll an endless list of its movies, then open a movie to see its details, its YouTube trailer and what
users wrote about it.

Built with Kotlin, Jetpack Compose and Material 3, following Clean Architecture with MVVM.

## Contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Setup](#setup)
- [Testing](#testing)
- [Code quality](#code-quality)

## Features

How each of the assessment's user stories is covered:

| # | User story | Where |
|---|------------|-------|
| 1 | See the list of official movie genres | **Home**: a grid of genre tiles from `genre/movie/list`. Each genre keeps the same color gradient across launches. |
| 2 | See the movies of a genre (discover by genre) | **Movie list**: opened from a genre tile. Loads `discover/movie?with_genres=…`, sorted by popularity, as a poster grid with ratings. |
| 3 | See a movie's primary information | **Movie detail**: backdrop, poster, title, release year, runtime, rating and vote count, genres, tagline and overview. |
| 4 | Read the user reviews of a movie | **Movie detail** previews the first reviews; **Reviews** lists all of them. Markdown and HTML in reviews are cleaned up, and long reviews expand with *Read more*. |
| 5 | Watch the movie's YouTube trailer | **Movie detail**: an embedded YouTube player, preferring the official trailer, then any trailer, then a teaser. *Open in YouTube* hands off to the YouTube app or browser. |
| 6 | Endless scrolling on movies and reviews | **Movie list** and **Reviews** load page by page with Paging 3 as you scroll, with pull-to-refresh and a retry row when a page fails. |
| 7 | Cover Positive and negative cases  | Every screen has loading, content, empty and error states with retry. |

## Tech stack

| Library | Why |
|---------|-----|
| **Kotlin** + **Coroutines/Flow** | Structured concurrency for network calls, and `StateFlow` as the single source of UI state. |
| **Jetpack Compose** + **Material 3** | Declarative UI that renders straight from state, with a themeable design system and built-in dark mode. |
| **Navigation Compose** (type-safe routes) | Routes are `@Serializable` classes, so arguments are checked at compile time and restored from `SavedStateHandle`. |
| **Hilt** (KSP) | Android's standard DI: compile-time checked graph and constructor injection into ViewModels. |
| **Retrofit** + **OkHttp** | Declarative, suspend-friendly HTTP client. OkHttp interceptors add the auth header and, in debug builds only, logging. |
| **kotlinx.serialization** | Kotlin-first JSON parsing that generates code at compile time instead of using reflection, so it works with R8 and understands Kotlin nullability and default values. |
| **Paging 3** | Endless scrolling with load states for refresh and append, retry, and a page cache that survives configuration changes. |
| **Coil 3** | Compose-native image loading built on coroutines, sharing the OkHttp stack. |
| **android-youtube-player** | Embeds YouTube's IFrame player in a view with lifecycle support; it needs neither a YouTube API key nor the YouTube app. |
| **JUnit 4**, **MockK**, **Turbine**, **kotlinx-coroutines-test** | Unit tests for coroutine and Flow code with a controllable clock. |
| **MockWebServer**, **paging-testing**, **Robolectric** | The real Retrofit stack against canned HTTP responses, `PagingData` snapshots, and `SavedStateHandle` route parsing on the JVM. |
| **Compose UI Test** | Instrumented tests of each screen's states and interactions. |
| **Detekt** (+ Compose rules), **Spotless** (ktlint) | Static analysis and consistent formatting, enforced from the command line. |

## Architecture

Clean Architecture in one `app` module, with MVVM in the presentation layer. Dependencies point inward
(`presentation → domain ← data`): the domain layer holds models, the repository interface and use cases, and
does not depend on Retrofit, DTOs or Compose.

### Package structure

```
com.app.cinevo
├── MainActivity.kt, MovieApp.kt
├── di/                   Hilt modules (network, repository bindings)
├── core/
│   ├── image/            TMDB image URL builder (poster, backdrop, avatar sizes)
│   ├── network/          ApiResult, ApiError, ApiException, safeApiCall, AuthInterceptor
│   └── ui/               Shared composables: poster card, section title, loading/empty/error views, error mapping
├── data/
│   ├── remote/           TmdbApi (Retrofit service)
│   │   └── dto/          @Serializable API models
│   ├── mapper/           DTO → domain mappers
│   ├── paging/           TmdbPagingSource, plus movie and review sources
│   └── repository/       MovieRepositoryImpl
├── domain/
│   ├── model/            Movie, MovieDetail, Genre, Review, Video, trailer selection
│   ├── repository/       MovieRepository interface
│   └── usecase/          One use case per operation
└── presentation/
    ├── navigation/       Type-safe routes and the NavHost
    ├── home/             Trending row and genre grid
    ├── movies/           Movies of a genre (paged)
    ├── detail/           Movie detail, trailer, review preview
    ├── reviews/          All reviews of a movie (paged)
    └── theme/            Colors, typography, shapes, spacing, genre gradients
```

## Setup

**Requirements:** Android Studio (latest stable), JDK 21 (Gradle provisions it through the daemon toolchain), and
Android SDK 37. The app runs on Android 8.0 (API 26) and later.

1. **Get a TMDB token.** Create a free account at [themoviedb.org](https://www.themoviedb.org/signup), then open
   [Settings → API](https://www.themoviedb.org/settings/api) and copy the **API Read Access Token** (the long v4
   token, not the short v3 "API Key").
2. **Add it to `local.properties`** in the project root.

   ```properties
   sdk.dir=/path/to/Android/sdk
   TMDB_API_KEY=your_tmdb_api_read_access_token
   ```
3. **Build and run.** Open the project in Android Studio and run the `app` configuration, or from the command line:

   ```bash
   ./gradlew installDebug        # build and install the debug app on a connected device or emulator
   ./gradlew assembleRelease     # minified release APK: app/build/outputs/apk/release/app-release.apk
   ```

## Testing

```bash
./gradlew testDebugUnitTest           # unit tests (report: app/build/reports/tests/testDebugUnitTest/index.html)
./gradlew connectedDebugAndroidTest   # UI tests; needs a connected device or running emulator
```

## Code quality

```bash
./gradlew detekt          # static analysis, including Compose rules
./gradlew spotlessCheck   # formatting check (ktlint)
./gradlew spotlessApply   # fix formatting
./gradlew lintDebug       # Android Lint
```

Detekt is configured in [`config/detekt/detekt.yml`](config/detekt/detekt.yml) and formatting in
[`.editorconfig`](.editorconfig).
