package com.app.cinevo.domain.usecase

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.repository.MovieRepository
import javax.inject.Inject

class GetGenresUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(): ApiResult<List<Genre>> = repository.getGenres()
    }
