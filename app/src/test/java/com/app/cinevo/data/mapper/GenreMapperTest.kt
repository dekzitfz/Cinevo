package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.GenreDto
import com.app.cinevo.domain.model.Genre
import org.junit.Assert.assertEquals
import org.junit.Test

class GenreMapperTest {
    @Test
    fun `maps id and name to domain genre`() {
        assertEquals(Genre(id = 28, name = "Action"), GenreDto(id = 28, name = "Action").toDomain())
    }

    @Test
    fun `keeps an empty name as is`() {
        assertEquals(Genre(id = 99, name = ""), GenreDto(id = 99, name = "").toDomain())
    }
}
