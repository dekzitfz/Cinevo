package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieMapperTest {
    @Test
    fun `maps every field to domain movie`() {
        val dto =
            MovieDto(
                id = 550,
                title = "Fight Club",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                voteAverage = 8.4,
                releaseDate = "1999-10-15",
            )

        assertEquals(
            Movie(
                id = 550,
                title = "Fight Club",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                voteAverage = 8.4,
                releaseDate = "1999-10-15",
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `keeps null optional fields as null`() {
        val movie = MovieDto(id = 1, title = "Untitled").toDomain()

        assertEquals(Movie(1, "Untitled", null, null, 0.0, null), movie)
    }

    @Test
    fun `turns blank optional fields into null`() {
        val movie =
            MovieDto(id = 1, title = "Untitled", posterPath = "", backdropPath = " ", releaseDate = "").toDomain()

        assertEquals(Movie(1, "Untitled", null, null, 0.0, null), movie)
    }
}
