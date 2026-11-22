package com.app.cinevo.presentation.genres

import androidx.annotation.StringRes
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre

sealed interface GenreUiState {
    data object Loading : GenreUiState

    data class Success(
        val genres: List<Genre>,
    ) : GenreUiState

    data object Empty : GenreUiState

    data class Error(
        @param:StringRes val message: Int,
        val type: ErrorType,
    ) : GenreUiState
}
