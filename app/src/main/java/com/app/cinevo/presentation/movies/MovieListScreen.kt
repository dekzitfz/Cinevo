package com.app.cinevo.presentation.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.core.ui.EmptyView
import com.app.cinevo.core.ui.ErrorView
import com.app.cinevo.core.ui.InlineErrorView
import com.app.cinevo.core.ui.LoadingView
import com.app.cinevo.core.ui.MoviePosterCard
import com.app.cinevo.core.ui.MoviePosterCardSkeleton
import com.app.cinevo.core.ui.SkeletonPulse
import com.app.cinevo.core.ui.fullWidthItem
import com.app.cinevo.core.ui.messageRes
import com.app.cinevo.core.ui.toErrorType
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.presentation.theme.CinevoTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MovieListScreen(
    onMovieClick: (movieId: Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    MovieListScreen(
        genreName = viewModel.genreName,
        movies = viewModel.movies.collectAsLazyPagingItems(),
        onMovieClick = onMovieClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun MovieListScreen(
    genreName: String,
    movies: LazyPagingItems<Movie>,
    onMovieClick: (movieId: Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = genreName,
                        modifier = Modifier.semantics { heading() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        // Hoisted above the loading/content switch: after rotation the paged list is briefly empty while the cached
        // pages are re-delivered, and a grid created inside the switch would come back scrolled to the top.
        val gridState = rememberLazyGridState()
        val spacing = CinevoTheme.spacing
        val layoutDirection = LocalLayoutDirection.current
        // The top inset is applied to the refresh box so its indicator appears below the app bar. The other insets
        // go inside the scrollable area so content can scroll behind the navigation bar.
        val contentPadding =
            PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection) + spacing.md,
                top = spacing.md,
                end = innerPadding.calculateEndPadding(layoutDirection) + spacing.md,
                bottom = innerPadding.calculateBottomPadding() + spacing.md,
            )
        PullToRefreshBox(
            isRefreshing = movies.loadState.refresh is LoadState.Loading && movies.itemCount > 0,
            onRefresh = movies::refresh,
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
        ) {
            MovieListContent(
                movies = movies,
                gridState = gridState,
                onMovieClick = onMovieClick,
                contentPadding = contentPadding,
            )
        }
    }
}

/** Picks what to show from the refresh state. Loaded movies stay visible while a pull-to-refresh is in flight. */
@Composable
private fun MovieListContent(
    movies: LazyPagingItems<Movie>,
    gridState: LazyGridState,
    onMovieClick: (movieId: Int) -> Unit,
    contentPadding: PaddingValues,
) {
    val refresh = movies.loadState.refresh
    when {
        refresh is LoadState.Error -> {
            ErrorView(
                message = stringResource(refresh.error.toErrorType().messageRes()),
                onRetry = movies::retry,
                modifier = Modifier.padding(contentPadding),
            )
        }

        movies.itemCount > 0 -> {
            MovieGrid(
                movies = movies,
                gridState = gridState,
                onMovieClick = onMovieClick,
                contentPadding = contentPadding,
            )
        }

        refresh is LoadState.Loading -> {
            SkeletonPulse(contentDescription = stringResource(R.string.movies_loading)) {
                PosterGrid(contentPadding = contentPadding, userScrollEnabled = false) {
                    items(count = 12) { MoviePosterCardSkeleton() }
                }
            }
        }

        else -> {
            EmptyView(message = stringResource(R.string.movies_empty), modifier = Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun MovieGrid(
    movies: LazyPagingItems<Movie>,
    gridState: LazyGridState,
    onMovieClick: (movieId: Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    PosterGrid(contentPadding = contentPadding, modifier = modifier, state = gridState) {
        items(count = movies.itemCount, key = movies.itemKey { it.id }) { index ->
            movies[index]?.let { movie -> MoviePosterCard(movie = movie, onClick = { onMovieClick(movie.id) }) }
        }
        when (movies.loadState.append) {
            is LoadState.Loading -> {
                fullWidthItem(key = "appendLoading") {
                    LoadingView(modifier = Modifier.padding(CinevoTheme.spacing.md))
                }
            }

            is LoadState.Error -> {
                fullWidthItem(key = "appendError") {
                    InlineErrorView(message = stringResource(R.string.movies_load_more_error), onRetry = movies::retry)
                }
            }

            is LoadState.NotLoading -> {}
        }
    }
}

@Composable
private fun PosterGrid(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.md),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

private val PreviewMovies =
    listOf(
        Movie(1, "The Long Night", null, 8.4),
        Movie(2, "Paper Harbor", null, 7.1),
        Movie(3, "A Very Long Title That Wraps Onto Two Lines", null, 6.6),
        Movie(4, "Echoes", null, 7.9),
    )

private val PreviewError = LoadState.Error(ApiException(ApiError.Network))

private fun previewLoadStates(
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
) = LoadStates(refresh = refresh, prepend = LoadState.NotLoading(endOfPaginationReached = true), append = append)

@Composable
private fun MovieListScreenPreview(pagingData: PagingData<Movie>) {
    // A StateFlow lets LazyPagingItems present static paging data on the first frame, which previews need.
    val movies = remember { MutableStateFlow(pagingData) }.collectAsLazyPagingItems()
    CinevoTheme {
        MovieListScreen(genreName = "Action", movies = movies, onMovieClick = {}, onBackClick = {})
    }
}

@PreviewLightDark
@Composable
private fun MovieListScreenLoadingPreview() {
    MovieListScreenPreview(PagingData.empty(previewLoadStates(refresh = LoadState.Loading)))
}

@PreviewLightDark
@Composable
private fun MovieListScreenLoadingMorePreview() {
    MovieListScreenPreview(PagingData.from(PreviewMovies, previewLoadStates(append = LoadState.Loading)))
}

@PreviewLightDark
@Composable
private fun MovieListScreenErrorPreview() {
    MovieListScreenPreview(PagingData.empty(previewLoadStates(refresh = PreviewError)))
}

@PreviewLightDark
@Composable
private fun MovieListScreenLoadMoreErrorPreview() {
    MovieListScreenPreview(PagingData.from(PreviewMovies, previewLoadStates(append = PreviewError)))
}

@PreviewLightDark
@Composable
private fun MovieListScreenEmptyPreview() {
    MovieListScreenPreview(PagingData.empty(previewLoadStates()))
}
