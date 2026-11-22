package com.app.cinevo.presentation.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.cinevo.R
import com.app.cinevo.core.ui.EmptyView
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.core.ui.ErrorView
import com.app.cinevo.core.ui.LoadingView
import com.app.cinevo.core.ui.fullWidthItem
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.presentation.theme.CinevoTheme
import com.app.cinevo.presentation.theme.GenreGradients

@Composable
fun HomeScreen(
    onMovieClick: (movieId: Int) -> Unit,
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onMovieClick = onMovieClick,
        onGenreClick = onGenreClick,
        onRetryTrending = viewModel::retryTrending,
        onRetryGenres = viewModel::retryGenres,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMovieClick: (movieId: Int) -> Unit,
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    onRetryTrending: () -> Unit,
    onRetryGenres: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        val spacing = CinevoTheme.spacing
        val layoutDirection = LocalLayoutDirection.current
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            // Scaffold padding goes inside the scrollable area so content can scroll behind the system bars.
            contentPadding =
                PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection) + spacing.md,
                    top = innerPadding.calculateTopPadding() + spacing.md,
                    end = innerPadding.calculateEndPadding(layoutDirection) + spacing.md,
                    bottom = innerPadding.calculateBottomPadding() + spacing.md,
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            fullWidthItem(key = "header") {
                Text(
                    text = stringResource(R.string.home_header),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (uiState.trending != SectionUiState.Empty) {
                fullWidthItem(key = "trendingTitle") { SectionTitle(text = R.string.trending_title) }
                fullWidthItem(key = "trending") {
                    TrendingSection(state = uiState.trending, onMovieClick = onMovieClick, onRetry = onRetryTrending)
                }
            }
            fullWidthItem(key = "genresTitle") { SectionTitle(text = R.string.genres_title) }
            genresSection(state = uiState.genres, onGenreClick = onGenreClick, onRetry = onRetryGenres)
        }
    }
}

private fun LazyGridScope.genresSection(
    state: SectionUiState<Genre>,
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        SectionUiState.Loading -> {
            fullWidthItem(key = "genresStatus") { LoadingView(modifier = Modifier.padding(CinevoTheme.spacing.xl)) }
        }

        SectionUiState.Empty -> {
            fullWidthItem(key = "genresStatus") { EmptyView(message = stringResource(R.string.genres_empty)) }
        }

        is SectionUiState.Error -> {
            fullWidthItem(key = "genresStatus") {
                ErrorView(message = stringResource(state.message), onRetry = onRetry)
            }
        }

        is SectionUiState.Success -> {
            items(items = state.items, key = { it.id }) { genre ->
                GenreCard(genre = genre, onClick = { onGenreClick(genre.id, genre.name) })
            }
        }
    }
}

@Composable
private fun SectionTitle(
    @StringRes text: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(text),
        modifier = modifier.padding(top = CinevoTheme.spacing.xs),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun GenreCard(
    genre: Genre,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(GenreGradients.forGenre(genre.id).brush)
                    .padding(CinevoTheme.spacing.md),
            contentAlignment = Alignment.BottomStart,
        ) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

private val PreviewMovies =
    listOf(
        Movie(1, "The Long Night", null, null, 8.4, "2026-10-02"),
        Movie(2, "Paper Harbor", null, null, 7.1, "2026-09-18"),
        Movie(3, "A Very Long Title That Wraps Onto Two Lines", null, null, 6.6, null),
        Movie(4, "Echoes", null, null, 7.9, "2026-11-01"),
    )

private val PreviewGenres =
    listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy"),
        Genre(80, "Crime"),
        Genre(18, "Drama"),
    )

private val PreviewError = SectionUiState.Error(R.string.error_no_connection, ErrorType.NoConnection)

@Composable
private fun HomeScreenPreview(uiState: HomeUiState) {
    CinevoTheme {
        HomeScreen(
            uiState = uiState,
            onMovieClick = {},
            onGenreClick = { _, _ -> },
            onRetryTrending = {},
            onRetryGenres = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreenPreview(HomeUiState())
}

@PreviewLightDark
@Composable
private fun HomeScreenSuccessPreview() {
    HomeScreenPreview(
        HomeUiState(trending = SectionUiState.Success(PreviewMovies), genres = SectionUiState.Success(PreviewGenres)),
    )
}

@PreviewLightDark
@Composable
private fun HomeScreenTrendingErrorPreview() {
    HomeScreenPreview(HomeUiState(trending = PreviewError, genres = SectionUiState.Success(PreviewGenres)))
}

@PreviewLightDark
@Composable
private fun HomeScreenGenresErrorPreview() {
    HomeScreenPreview(HomeUiState(trending = SectionUiState.Success(PreviewMovies), genres = PreviewError))
}

@PreviewLightDark
@Composable
private fun HomeScreenEmptyPreview() {
    HomeScreenPreview(HomeUiState(trending = SectionUiState.Empty, genres = SectionUiState.Empty))
}
