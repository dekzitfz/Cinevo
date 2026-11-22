package com.app.cinevo.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Genres

@Serializable
data class MovieList(
    val genreId: Int,
    val genreName: String,
)
