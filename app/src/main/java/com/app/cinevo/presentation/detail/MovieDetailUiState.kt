package com.app.cinevo.presentation.detail

import androidx.annotation.StringRes
import com.app.cinevo.domain.model.MovieDetail

sealed interface MovieDetailUiState {
    data object Loading : MovieDetailUiState

    data class Success(
        val movie: MovieDetail,
    ) : MovieDetailUiState

    /** [canRetry] is false when retrying cannot help, such as for a movie that does not exist. */
    data class Error(
        @param:StringRes val message: Int,
        val canRetry: Boolean,
    ) : MovieDetailUiState
}
