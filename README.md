# Cinevo

An Android app for browsing movies from [TMDB](https://www.themoviedb.org/): pick a genre, scroll an endless
list of movies for that genre, and open a movie to see its details, user reviews and YouTube trailer.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose (type-safe routes)
- Hilt (KSP)
- Retrofit, OkHttp, kotlinx.serialization
- Paging 3
- Coil 3
- JUnit 4, MockK, Turbine, MockWebServer, kotlinx-coroutines-test, Paging testing, Robolectric
- Detekt, Spotless (ktlint)

All versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Setup

Requirements: Android Studio (latest stable), JDK 17+, Android SDK 37.

1. Clone the repository.
2. Add your TMDB key to `local.properties`.
3. Build and run: `./gradlew installDebug`

### API key

The app authenticates with a TMDB **API Read Access Token** (v4 auth, sent as `Authorization: Bearer <token>`).
Get one at <https://www.themoviedb.org/settings/api>, then add it to `local.properties`:

```properties
TMDB_API_KEY=your_tmdb_read_access_token
```

The value is exposed to the app as `BuildConfig.TMDB_API_KEY`. `local.properties` is gitignored; never commit the key.

## Architecture

Clean Architecture with MVVM in the presentation layer, in a single `app` module:

```
com.app.cinevo
├── di/                  Hilt modules
├── core/
│   ├── image/           TMDB image URL builder
│   ├── network/         ApiResult, safeApiCall, interceptors
│   └── ui/              Shared composables
├── data/
│   ├── remote/          TmdbApi (Retrofit service)
│   │   └── dto/         API DTOs
│   ├── paging/          PagingSources
│   ├── mapper/          DTO → domain mappers
│   └── repository/      Repository implementations
├── domain/
│   ├── model/           Domain models
│   ├── repository/      Repository interfaces
│   └── usecase/         Use cases
└── presentation/
    ├── navigation/      Type-safe nav graph
    ├── home/            Home screen: trending movies and genres
    ├── movies/          Movies-by-genre screen
    ├── detail/          Movie detail screen
    └── theme/           Material 3 theme
```

Dependencies point inward: `presentation → domain ← data`. The domain layer has no Android or network
dependencies. Network calls are wrapped with `safeApiCall`, which returns `ApiResult.Success` or
`ApiResult.Error` with a typed `ApiError` (`Network`, `Http(code)` or `Unknown`).

## Running checks and tests

```bash
./gradlew testDebugUnitTest   # unit tests
./gradlew detekt              # static analysis
./gradlew spotlessCheck       # formatting (ktlint)
./gradlew spotlessApply       # auto-fix formatting
```
