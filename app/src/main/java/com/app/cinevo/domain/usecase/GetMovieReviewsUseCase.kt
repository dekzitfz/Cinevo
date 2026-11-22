package com.app.cinevo.domain.usecase

import androidx.paging.PagingData
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieReviewsUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        operator fun invoke(movieId: Int): Flow<PagingData<Review>> = repository.getMovieReviews(movieId)
    }
