package com.app.cinevo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One page of a TMDB list, such as `discover/movie`, `movie/{movie_id}/reviews` or an appended `reviews` block. */
@Serializable
data class PagedResponseDto<T>(
    val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)
