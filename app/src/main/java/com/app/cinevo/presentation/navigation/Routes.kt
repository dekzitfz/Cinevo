package com.app.cinevo.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data class MovieList(
    val genreId: Int,
    val genreName: String,
)

@Serializable
data class MovieDetail(
    val movieId: Int,
)

@Serializable
data class Reviews(
    val movieId: Int,
    val movieTitle: String,
)
