package com.app.cinevo.domain.usecase

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.repository.MovieRepository
import javax.inject.Inject

class GetTrendingMoviesUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(): ApiResult<List<Movie>> = repository.getTrendingMovies()
    }
