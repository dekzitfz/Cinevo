package com.app.cinevo.presentation.genres

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
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.presentation.theme.CinevoTheme
import com.app.cinevo.presentation.theme.GenreGradients

@Composable
fun GenreScreen(
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GenreScreen(
        uiState = uiState,
        onGenreClick = onGenreClick,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun GenreScreen(
    uiState: GenreUiState,
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        when (uiState) {
            GenreUiState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }

            GenreUiState.Empty -> {
                EmptyView(message = stringResource(R.string.genres_empty), modifier = Modifier.padding(innerPadding))
            }

            is GenreUiState.Error -> {
                ErrorView(
                    message = stringResource(uiState.message),
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is GenreUiState.Success -> {
                GenreGrid(state = uiState, onGenreClick = onGenreClick, innerPadding = innerPadding)
            }
        }
    }
}

@Composable
private fun GenreGrid(
    state: GenreUiState.Success,
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = CinevoTheme.spacing
    val layoutDirection = LocalLayoutDirection.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        // Scaffold padding goes inside the scrollable area so cards can scroll behind the system bars.
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
        items(items = state.genres, key = { it.id }) { genre ->
            GenreCard(genre = genre, onClick = { onGenreClick(genre.id, genre.name) })
        }
    }
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

private val PreviewGenres =
    listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(16, "Animation"),
        Genre(35, "Comedy"),
        Genre(80, "Crime"),
        Genre(18, "Drama"),
    )

@PreviewLightDark
@Composable
private fun GenreScreenLoadingPreview() {
    CinevoTheme {
        GenreScreen(uiState = GenreUiState.Loading, onGenreClick = { _, _ -> }, onRetry = {})
    }
}

@PreviewLightDark
@Composable
private fun GenreScreenSuccessPreview() {
    CinevoTheme {
        GenreScreen(uiState = GenreUiState.Success(PreviewGenres), onGenreClick = { _, _ -> }, onRetry = {})
    }
}

@PreviewLightDark
@Composable
private fun GenreScreenEmptyPreview() {
    CinevoTheme {
        GenreScreen(uiState = GenreUiState.Empty, onGenreClick = { _, _ -> }, onRetry = {})
    }
}

@PreviewLightDark
@Composable
private fun GenreScreenErrorPreview() {
    CinevoTheme {
        GenreScreen(
            uiState = GenreUiState.Error(R.string.error_no_connection, ErrorType.NoConnection),
            onGenreClick = { _, _ -> },
            onRetry = {},
        )
    }
}
