package com.app.cinevo.domain.model

import java.time.Instant
import java.time.LocalDate

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: LocalDate?,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val trailer: Video?,
    /** The first few reviews; [totalReviews] counts all of them. */
    val reviews: List<Review>,
    val totalReviews: Int,
)

data class Video(
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: Instant?,
)

data class Review(
    val id: String,
    val author: String,
    /** Full image URL, already resolved from TMDB's avatar path. */
    val avatarUrl: String?,
    val rating: Double?,
    val content: String,
    val createdAt: Instant?,
)
