package com.app.cinevo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.cinevo.R
import com.app.cinevo.core.ui.InlineErrorView
import com.app.cinevo.core.ui.MoviePosterCard
import com.app.cinevo.core.ui.MoviePosterCardSkeleton
import com.app.cinevo.core.ui.SkeletonPulse
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.presentation.theme.CinevoTheme

private val PosterWidth = 120.dp

/** Content of the trending section below its title. Draws nothing for [SectionUiState.Empty]. */
@Composable
internal fun TrendingSection(
    state: SectionUiState<Movie>,
    onMovieClick: (movieId: Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        SectionUiState.Loading -> {
            TrendingSkeletonRow(modifier = modifier)
        }

        SectionUiState.Empty -> {}

        is SectionUiState.Error -> {
            InlineErrorView(message = stringResource(state.message), onRetry = onRetry, modifier = modifier)
        }

        is SectionUiState.Success -> {
            TrendingRow(state = state, onMovieClick = onMovieClick, modifier = modifier)
        }
    }
}

@Composable
private fun TrendingRow(
    state: SectionUiState.Success<Movie>,
    onMovieClick: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm),
    ) {
        items(items = state.items, key = { it.id }) { movie ->
            MoviePosterCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                modifier = Modifier.width(PosterWidth),
            )
        }
    }
}

@Composable
private fun TrendingSkeletonRow(modifier: Modifier = Modifier) {
    SkeletonPulse(contentDescription = stringResource(R.string.trending_loading), modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm),
            userScrollEnabled = false,
        ) {
            items(count = 4) { MoviePosterCardSkeleton(modifier = Modifier.width(PosterWidth)) }
        }
    }
}
