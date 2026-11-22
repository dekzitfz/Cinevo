package com.app.cinevo.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.ui.messageRes
import com.app.cinevo.core.ui.toErrorType
import com.app.cinevo.domain.usecase.GetMovieDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import javax.inject.Inject
import com.app.cinevo.presentation.navigation.MovieDetail as MovieDetailRoute

@HiltViewModel
class MovieDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getMovieDetail: GetMovieDetailUseCase,
    ) : ViewModel() {
        private val movieId = savedStateHandle.toRoute<MovieDetailRoute>().movieId

        private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
        val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        init {
            load()
        }

        fun retry() = load()

        private fun load() {
            loadJob?.cancel()
            _uiState.value = MovieDetailUiState.Loading
            loadJob =
                viewModelScope.launch {
                    _uiState.value =
                        when (val result = getMovieDetail(movieId)) {
                            is ApiResult.Success -> MovieDetailUiState.Success(result.data)
                            is ApiResult.Error -> result.error.toUiState()
                        }
                }
        }

        private fun ApiError.toUiState(): MovieDetailUiState.Error =
            if (this == ApiError.Http(HTTP_NOT_FOUND)) {
                MovieDetailUiState.Error(message = R.string.movie_not_found, canRetry = false)
            } else {
                MovieDetailUiState.Error(message = toErrorType().messageRes(), canRetry = true)
            }
    }
