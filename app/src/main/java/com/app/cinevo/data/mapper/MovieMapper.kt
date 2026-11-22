package com.app.cinevo.data.mapper

import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.domain.model.Movie

/** TMDB sends missing optional values as either null or "", so both become null in the domain model. */
fun MovieDto.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath.nullIfBlank(),
        backdropPath = backdropPath.nullIfBlank(),
        voteAverage = voteAverage,
        releaseDate = releaseDate.nullIfBlank(),
    )

internal fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
