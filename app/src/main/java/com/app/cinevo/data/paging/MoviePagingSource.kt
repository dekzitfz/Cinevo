package com.app.cinevo.data.paging

import com.app.cinevo.data.mapper.toDomain
import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.domain.model.Movie

/** Pages through a TMDB movie list endpoint, such as `discover/movie`. */
class MoviePagingSource(
    fetchPage: suspend (page: Int) -> PagedResponseDto<MovieDto>,
) : TmdbPagingSource<MovieDto, Movie>(fetchPage, idOf = MovieDto::id, map = MovieDto::toDomain)
