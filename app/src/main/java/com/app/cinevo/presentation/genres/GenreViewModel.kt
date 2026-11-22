package com.app.cinevo.presentation.genres

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.core.ui.toErrorType
import com.app.cinevo.domain.usecase.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenreViewModel
    @Inject
    constructor(
        private val getGenres: GetGenresUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<GenreUiState>(GenreUiState.Loading)
        val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        init {
            load()
        }

        fun retry() = load()

        private fun load() {
            loadJob?.cancel()
            _uiState.value = GenreUiState.Loading
            loadJob =
                viewModelScope.launch {
                    _uiState.value =
                        when (val result = getGenres()) {
                            is ApiResult.Success -> {
                                if (result.data.isEmpty()) GenreUiState.Empty else GenreUiState.Success(result.data)
                            }

                            is ApiResult.Error -> {
                                val type = result.error.toErrorType()
                                GenreUiState.Error(message = type.messageRes(), type = type)
                            }
                        }
                }
        }

        @StringRes
        private fun ErrorType.messageRes(): Int =
            when (this) {
                ErrorType.NoConnection -> R.string.error_no_connection
                ErrorType.Server -> R.string.error_server
                ErrorType.Unauthorized -> R.string.error_unauthorized
                ErrorType.Unknown -> R.string.error_unknown
            }
    }
