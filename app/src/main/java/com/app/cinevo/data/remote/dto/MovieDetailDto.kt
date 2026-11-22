package com.app.cinevo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `movie/{movie_id}` with `append_to_response=videos,reviews`. */
@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val tagline: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val genres: List<GenreDto> = emptyList(),
    val videos: VideoListDto = VideoListDto(),
    val reviews: PagedResponseDto<ReviewDto> = PagedResponseDto(),
)

@Serializable
data class VideoListDto(
    val results: List<VideoDto> = emptyList(),
)

@Serializable
data class VideoDto(
    val key: String,
    val site: String,
    val type: String,
    val name: String = "",
    val official: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable
data class ReviewDto(
    val id: String,
    val author: String,
    @SerialName("author_details") val authorDetails: AuthorDetailsDto? = null,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AuthorDetailsDto(
    val rating: Double? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
)
