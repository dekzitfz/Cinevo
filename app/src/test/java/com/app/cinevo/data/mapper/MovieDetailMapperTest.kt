package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.AuthorDetailsDto
import com.app.cinevo.data.remote.dto.GenreDto
import com.app.cinevo.data.remote.dto.MovieDetailDto
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.data.remote.dto.VideoDto
import com.app.cinevo.data.remote.dto.VideoListDto
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.model.Video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MovieDetailMapperTest {
    private fun review(
        id: String,
        avatarPath: String? = null,
    ) = ReviewDto(
        id = id,
        author = "Author $id",
        authorDetails = AuthorDetailsDto(avatarPath = avatarPath),
        content = "Content $id",
    )

    private val fullDto =
        MovieDetailDto(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = "An insomniac office worker...",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "1999-10-15",
            runtime = 139,
            voteAverage = 8.4,
            voteCount = 30_000,
            genres = listOf(GenreDto(18, "Drama")),
            videos =
                VideoListDto(
                    listOf(
                        VideoDto("teaser", "YouTube", "Teaser", true, "2019-01-01T00:00:00.000Z"),
                        VideoDto("trailer", "YouTube", "Trailer", true, "2018-01-01T00:00:00.000Z"),
                    ),
                ),
            reviews =
                PagedResponseDto(
                    results =
                        listOf(
                            ReviewDto(
                                id = "r1",
                                author = "Jane",
                                authorDetails = AuthorDetailsDto(rating = 9.0, avatarPath = "/jane.jpg"),
                                content = "Great.",
                                createdAt = "2020-05-01T10:15:30.123Z",
                            ),
                        ),
                    totalResults = 12,
                ),
        )

    private val fullDetail =
        MovieDetail(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = "An insomniac office worker...",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = LocalDate.of(1999, 10, 15),
            runtimeMinutes = 139,
            voteAverage = 8.4,
            voteCount = 30_000,
            genres = listOf(Genre(18, "Drama")),
            trailer =
                Video(
                    key = "trailer",
                    site = "YouTube",
                    type = "Trailer",
                    official = true,
                    publishedAt = Instant.parse("2018-01-01T00:00:00Z"),
                ),
            reviews =
                listOf(
                    Review(
                        id = "r1",
                        author = "Jane",
                        avatarUrl = "https://image.tmdb.org/t/p/w185/jane.jpg",
                        rating = 9.0,
                        content = "Great.",
                        createdAt = Instant.parse("2020-05-01T10:15:30.123Z"),
                    ),
                ),
            totalReviews = 12,
        )

    @Test
    fun `maps every field to the domain model`() {
        assertEquals(fullDetail, fullDto.toDomain())
    }

    @Test
    fun `missing optional fields become null or empty`() {
        val movie = MovieDetailDto(id = 1, title = "Untitled").toDomain()

        assertEquals(
            MovieDetail(1, "Untitled", null, null, null, null, null, null, 0.0, 0, emptyList(), null, emptyList(), 0),
            movie,
        )
    }

    @Test
    fun `blank text, zero runtime and unparseable dates become null`() {
        val movie =
            MovieDetailDto(
                id = 1,
                title = "Untitled",
                tagline = "",
                overview = " ",
                posterPath = "",
                backdropPath = "",
                releaseDate = "",
                runtime = 0,
            ).toDomain()

        assertNull(movie.tagline)
        assertNull(movie.overview)
        assertNull(movie.posterPath)
        assertNull(movie.backdropPath)
        assertNull(movie.releaseDate)
        assertNull(movie.runtimeMinutes)
        assertNull(MovieDetailDto(id = 1, title = "Untitled", releaseDate = "2024-13-45").toDomain().releaseDate)
    }

    @Test
    fun `review without author details or dates maps to nulls`() {
        val review = ReviewDto(id = "r1", author = "Jane", content = "Great.", createdAt = "not a date").toDomain()

        assertEquals(Review("r1", "Jane", null, null, "Great.", null), review)
    }

    @Test
    fun `video with a malformed date keeps the video without a date`() {
        val video = VideoDto("k", "YouTube", "Trailer", publishedAt = "yesterday").toDomain()

        assertNull(video.publishedAt)
        assertEquals("k", video.key)
    }

    @Test
    fun `avatar path with a full url after a leading slash is used as that url`() {
        assertEquals(
            "https://secure.gravatar.com/avatar/abc.jpg",
            review("r1", avatarPath = "/https://secure.gravatar.com/avatar/abc.jpg").toDomain().avatarUrl,
        )
        assertEquals(
            "http://www.gravatar.com/avatar/abc.jpg",
            review("r1", avatarPath = "/http://www.gravatar.com/avatar/abc.jpg").toDomain().avatarUrl,
        )
    }

    @Test
    fun `avatar path that is already a full url is kept`() {
        assertEquals(
            "https://secure.gravatar.com/avatar/abc.jpg",
            review("r1", avatarPath = "https://secure.gravatar.com/avatar/abc.jpg").toDomain().avatarUrl,
        )
    }

    @Test
    fun `avatar path that is a tmdb file builds a tmdb image url`() {
        assertEquals(
            "https://image.tmdb.org/t/p/w185/avatar.jpg",
            review("r1", avatarPath = "/avatar.jpg").toDomain().avatarUrl,
        )
    }

    @Test
    fun `null or blank avatar path has no avatar`() {
        assertNull(review("r1", avatarPath = null).toDomain().avatarUrl)
        assertNull(review("r1", avatarPath = "").toDomain().avatarUrl)
    }

    @Test
    fun `keeps only the first reviews but counts them all`() {
        val movie =
            MovieDetailDto(
                id = 1,
                title = "Popular",
                reviews = PagedResponseDto(results = (1..5).map { review("r$it") }, totalResults = 42),
            ).toDomain()

        assertEquals(listOf("r1", "r2", "r3"), movie.reviews.map { it.id })
        assertEquals(42, movie.totalReviews)
    }
}
