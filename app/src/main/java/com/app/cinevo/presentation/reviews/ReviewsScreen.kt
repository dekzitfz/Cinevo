package com.app.cinevo.presentation.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.app.cinevo.core.ui.SkeletonPulse
import com.app.cinevo.core.ui.messageRes
import com.app.cinevo.core.ui.toErrorType
import com.app.cinevo.domain.model.Review
import com.app.cinevo.presentation.theme.CinevoTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

@Composable
fun ReviewsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewsViewModel = hiltViewModel(),
) {
    ReviewsScreen(
        movieTitle = viewModel.movieTitle,
        reviews = viewModel.reviews.collectAsLazyPagingItems(),
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

/** All reviews of a movie, loaded page by page as the list scrolls. */
@Composable
fun ReviewsScreen(
    movieTitle: String,
    reviews: LazyPagingItems<Review>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.semantics(mergeDescendants = true) { heading() }) {
                        Text(text = stringResource(R.string.reviews_title))
                        Text(
                            text = movieTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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
        // Hoisted above the loading/content switch for the same reason as on the movie list: the list must keep its
        // scroll position across rotation while the cached pages are re-delivered.
        val listState = rememberLazyListState()
        val spacing = CinevoTheme.spacing
        val layoutDirection = LocalLayoutDirection.current
        // As on the movie list: the top inset goes on the refresh box so its indicator appears below the app bar,
        // the others inside the list so reviews can scroll behind the navigation bar.
        val contentPadding =
            PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection) + spacing.md,
                top = spacing.md,
                end = innerPadding.calculateEndPadding(layoutDirection) + spacing.md,
                bottom = innerPadding.calculateBottomPadding() + spacing.md,
            )
        PullToRefreshBox(
            isRefreshing = reviews.loadState.refresh is LoadState.Loading && reviews.itemCount > 0,
            onRefresh = reviews::refresh,
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
        ) {
            ReviewsContent(reviews = reviews, listState = listState, contentPadding = contentPadding)
        }
    }
}

/** Picks what to show from the refresh state. Loaded reviews stay visible while a pull-to-refresh is in flight. */
@Composable
private fun ReviewsContent(
    reviews: LazyPagingItems<Review>,
    listState: LazyListState,
    contentPadding: PaddingValues,
) {
    val refresh = reviews.loadState.refresh
    when {
        refresh is LoadState.Error -> {
            ErrorView(
                message = stringResource(refresh.error.toErrorType().messageRes()),
                onRetry = reviews::retry,
                modifier = Modifier.padding(contentPadding),
            )
        }

        reviews.itemCount > 0 -> {
            ReviewList(reviews = reviews, listState = listState, contentPadding = contentPadding)
        }

        refresh is LoadState.Loading -> {
            SkeletonPulse(contentDescription = stringResource(R.string.reviews_loading)) {
                ReviewColumn(contentPadding = contentPadding, userScrollEnabled = false) {
                    items(count = 4) { ReviewCardSkeleton() }
                }
            }
        }

        else -> {
            EmptyView(message = stringResource(R.string.reviews_empty), modifier = Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun ReviewList(
    reviews: LazyPagingItems<Review>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    ReviewColumn(contentPadding = contentPadding, modifier = modifier, state = listState) {
        items(count = reviews.itemCount, key = reviews.itemKey { it.id }) { index ->
            reviews[index]?.let { review -> ReviewCard(review = review, expandable = true) }
        }
        when (reviews.loadState.append) {
            is LoadState.Loading -> {
                item(key = "appendLoading") { LoadingView(modifier = Modifier.padding(CinevoTheme.spacing.md)) }
            }

            is LoadState.Error -> {
                item(key = "appendError") {
                    InlineErrorView(
                        message = stringResource(R.string.reviews_load_more_error),
                        onRetry = reviews::retry,
                    )
                }
            }

            is LoadState.NotLoading -> {}
        }
    }
}

@Composable
private fun ReviewColumn(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.md),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

private val PreviewReviews =
    listOf(
        Review(
            id = "r1",
            author = "Jamie Rivera",
            avatarUrl = null,
            rating = 9.0,
            content =
                "Tense from the first frame to the last. The sound design alone makes this worth seeing on the " +
                    "biggest screen you can find, and the final act lands every beat it set up. ".repeat(n = 4),
            createdAt = Instant.parse("2026-10-05T12:00:00Z"),
        ),
        Review("r2", "moviebuff", null, null, "Slow start, _great_ payoff.", null),
        Review("r3", "Ana", null, 6.0, "Good, not great.", Instant.parse("2026-10-09T08:00:00Z")),
    )

private val PreviewError = LoadState.Error(ApiException(ApiError.Network))

private fun previewLoadStates(
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
) = LoadStates(refresh = refresh, prepend = LoadState.NotLoading(endOfPaginationReached = true), append = append)

@Composable
private fun ReviewsScreenPreview(pagingData: PagingData<Review>) {
    // A StateFlow lets LazyPagingItems present static paging data on the first frame, which previews need.
    val reviews = remember { MutableStateFlow(pagingData) }.collectAsLazyPagingItems()
    CinevoTheme {
        ReviewsScreen(movieTitle = "The Long Night", reviews = reviews, onBackClick = {})
    }
}

@PreviewLightDark
@Composable
private fun ReviewsScreenSuccessPreview() {
    ReviewsScreenPreview(PagingData.from(PreviewReviews, previewLoadStates()))
}

@PreviewLightDark
@Composable
private fun ReviewsScreenLoadingPreview() {
    ReviewsScreenPreview(PagingData.empty(previewLoadStates(refresh = LoadState.Loading)))
}

@PreviewLightDark
@Composable
private fun ReviewsScreenLoadingMorePreview() {
    ReviewsScreenPreview(PagingData.from(PreviewReviews, previewLoadStates(append = LoadState.Loading)))
}

@PreviewLightDark
@Composable
private fun ReviewsScreenErrorPreview() {
    ReviewsScreenPreview(PagingData.empty(previewLoadStates(refresh = PreviewError)))
}

@PreviewLightDark
@Composable
private fun ReviewsScreenLoadMoreErrorPreview() {
    ReviewsScreenPreview(PagingData.from(PreviewReviews, previewLoadStates(append = PreviewError)))
}

@PreviewLightDark
@Composable
private fun ReviewsScreenEmptyPreview() {
    ReviewsScreenPreview(PagingData.empty(previewLoadStates()))
}
