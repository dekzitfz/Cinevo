package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.GenreDto
import com.app.cinevo.domain.model.Genre

fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)
