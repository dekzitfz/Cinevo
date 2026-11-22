package com.app.cinevo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenreListResponseDto(
    val genres: List<GenreDto>,
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)
