package com.app.cinevo.data.paging

import com.app.cinevo.data.mapper.toDomain
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.domain.model.Review

/** Pages through the reviews of a movie, `movie/{movie_id}/reviews`. */
class ReviewPagingSource(
    fetchPage: suspend (page: Int) -> PagedResponseDto<ReviewDto>,
) : TmdbPagingSource<ReviewDto, Review>(fetchPage, idOf = ReviewDto::id, map = ReviewDto::toDomain)
