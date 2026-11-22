package com.app.cinevo.domain.usecase

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieDetailUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(movieId: Int): ApiResult<MovieDetail> = repository.getMovieDetail(movieId)
    }
