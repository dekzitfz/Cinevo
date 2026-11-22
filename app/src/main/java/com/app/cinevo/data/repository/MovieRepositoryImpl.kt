package com.app.cinevo.data.repository

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.network.safeApiCall
import com.app.cinevo.data.mapper.toDomain
import com.app.cinevo.data.remote.TmdbApi
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl
    @Inject
    constructor(
        private val api: TmdbApi,
    ) : MovieRepository {
        override suspend fun getGenres(): ApiResult<List<Genre>> =
            safeApiCall { api.getMovieGenres().genres.map { it.toDomain() } }

        override suspend fun getTrendingMovies(): ApiResult<List<Movie>> =
            safeApiCall { api.getTrendingMovies().results.map { it.toDomain() } }
    }
