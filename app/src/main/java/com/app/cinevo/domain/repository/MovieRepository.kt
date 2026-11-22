package com.app.cinevo.domain.repository

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie

interface MovieRepository {
    suspend fun getGenres(): ApiResult<List<Genre>>

    suspend fun getTrendingMovies(): ApiResult<List<Movie>>
}
