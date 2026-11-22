package com.app.cinevo.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.ui.messageRes
import com.app.cinevo.core.ui.toErrorType
import com.app.cinevo.domain.usecase.GetGenresUseCase
import com.app.cinevo.domain.usecase.GetTrendingMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getTrendingMovies: GetTrendingMoviesUseCase,
        private val getGenres: GetGenresUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        private var trendingJob: Job? = null
        private var genresJob: Job? = null

        init {
            loadTrending()
            loadGenres()
        }

        fun retryTrending() = loadTrending()

        fun retryGenres() = loadGenres()

        private fun loadTrending() {
            trendingJob = loadSection(trendingJob, { getTrendingMovies() }) { copy(trending = it) }
        }

        private fun loadGenres() {
            genresJob = loadSection(genresJob, { getGenres() }) { copy(genres = it) }
        }

        private fun <T> loadSection(
            previous: Job?,
            fetch: suspend () -> ApiResult<List<T>>,
            reduce: HomeUiState.(SectionUiState<T>) -> HomeUiState,
        ): Job {
            previous?.cancel()
            _uiState.update { it.reduce(SectionUiState.Loading) }
            return viewModelScope.launch {
                val section = fetch().toSectionUiState()
                _uiState.update { it.reduce(section) }
            }
        }

        private fun <T> ApiResult<List<T>>.toSectionUiState(): SectionUiState<T> =
            when (this) {
                is ApiResult.Success -> {
                    if (data.isEmpty()) SectionUiState.Empty else SectionUiState.Success(data)
                }

                is ApiResult.Error -> {
                    val type = error.toErrorType()
                    SectionUiState.Error(message = type.messageRes(), type = type)
                }
            }
    }
