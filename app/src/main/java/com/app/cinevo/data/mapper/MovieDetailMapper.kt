package com.app.cinevo.data.mapper

import com.app.cinevo.core.image.TmdbImageUrl
import com.app.cinevo.data.remote.dto.MovieDetailDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.data.remote.dto.VideoDto
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.model.Video
import com.app.cinevo.domain.model.selectTrailer
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** TMDB sends unknown runtimes as 0 or null and missing text and dates as null or "", so all become null. */
fun MovieDetailDto.toDomain(): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        tagline = tagline.nullIfBlank(),
        overview = overview.nullIfBlank(),
        posterPath = posterPath.nullIfBlank(),
        backdropPath = backdropPath.nullIfBlank(),
        releaseDate = releaseDate.parseOrNull(LocalDate::parse),
        runtimeMinutes = runtime?.takeIf { it > 0 },
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.toDomain() },
        trailer = selectTrailer(videos.results.map { it.toDomain() }),
        // The detail screen previews a few reviews; the rest are reached through the reviews screen.
        reviews = reviews.results.take(n = 3).map { it.toDomain() },
        totalReviews = reviews.totalResults,
    )

fun VideoDto.toDomain(): Video =
    Video(
        key = key,
        name = name,
        site = site,
        type = type,
        official = official,
        publishedAt = publishedAt.parseOrNull(Instant::parse),
    )

fun ReviewDto.toDomain(): Review =
    Review(
        id = id,
        author = author,
        avatarUrl = avatarUrl(authorDetails?.avatarPath),
        rating = authorDetails?.rating,
        content = content,
        createdAt = createdAt.parseOrNull(Instant::parse),
    )

/**
 * TMDB stores external avatars (such as Gravatar) as the full URL behind a leading slash, for example
 * `/https://secure.gravatar.com/avatar/abc.jpg`. Those are used as-is; anything else is a TMDB image path.
 */
private fun avatarUrl(avatarPath: String?): String? {
    val path = avatarPath.nullIfBlank() ?: return null
    val withoutSlash = path.removePrefix("/")
    return if (withoutSlash.startsWith("http://") || withoutSlash.startsWith("https://")) {
        withoutSlash
    } else {
        TmdbImageUrl.profile(path)
    }
}

private fun <T> String?.parseOrNull(parse: (String) -> T): T? =
    nullIfBlank()?.let {
        try {
            parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }
