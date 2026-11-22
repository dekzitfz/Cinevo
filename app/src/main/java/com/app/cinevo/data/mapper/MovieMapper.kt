package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.domain.model.Movie

/** TMDB sends a missing poster as either null or "", so both become null in the domain model. */
fun MovieDto.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath.nullIfBlank(),
        voteAverage = voteAverage,
    )

internal fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
