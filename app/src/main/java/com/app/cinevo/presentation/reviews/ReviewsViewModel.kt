package com.app.cinevo.presentation.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.usecase.GetMovieReviewsUseCase
import com.app.cinevo.presentation.navigation.Reviews
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getMovieReviews: GetMovieReviewsUseCase,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<Reviews>()

        val movieTitle: String = route.movieTitle

        val reviews: Flow<PagingData<Review>> = getMovieReviews(route.movieId).cachedIn(viewModelScope)
    }
