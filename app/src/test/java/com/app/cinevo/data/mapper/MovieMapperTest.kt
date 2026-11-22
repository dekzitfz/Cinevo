package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {
    @Test
    fun `maps every field to domain movie`() {
        val dto =
            MovieDto(
                id = 550,
                title = "Fight Club",
                posterPath = "/poster.jpg",
                voteAverage = 8.4,
            )

        assertEquals(
            Movie(
                id = 550,
                title = "Fight Club",
                posterPath = "/poster.jpg",
                voteAverage = 8.4,
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `keeps null optional fields as null`() {
        val movie = MovieDto(id = 1, title = "Untitled").toDomain()

        assertEquals(Movie(1, "Untitled", null, 0.0), movie)
    }

    @Test
    fun `turns a blank poster path into null`() {
        assertNull(MovieDto(id = 1, title = "Untitled", posterPath = "").toDomain().posterPath)
        assertNull(MovieDto(id = 1, title = "Untitled", posterPath = " ").toDomain().posterPath)
    }
}
