package com.app.cinevo.presentation.home

import androidx.annotation.StringRes
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie

/** Each section loads and fails on its own, so one never hides or blocks the other. */
data class HomeUiState(
    val trending: SectionUiState<Movie> = SectionUiState.Loading,
    val genres: SectionUiState<Genre> = SectionUiState.Loading,
)

sealed interface SectionUiState<out T> {
    data object Loading : SectionUiState<Nothing>

    data class Success<T>(
        val items: List<T>,
    ) : SectionUiState<T>

    data object Empty : SectionUiState<Nothing>

    data class Error(
        @param:StringRes val message: Int,
        val type: ErrorType,
    ) : SectionUiState<Nothing>
}
