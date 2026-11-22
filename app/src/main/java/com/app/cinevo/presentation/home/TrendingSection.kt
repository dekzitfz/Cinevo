package com.app.cinevo.presentation.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.app.cinevo.core.image.TmdbImageUrl
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
            InlineError(message = stringResource(state.message), onRetry = onRetry, modifier = modifier)
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
            TrendingMovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
        }
    }
}

@Composable
private fun TrendingMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholder = painterResource(R.drawable.ic_poster_placeholder)
    Column(
        modifier = modifier.width(PosterWidth).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs),
    ) {
        Box {
            AsyncImage(
                model = TmdbImageUrl.poster(movie.posterPath),
                contentDescription = null,
                modifier = Modifier.posterFrame(MaterialTheme.shapes.medium, MaterialTheme.colorScheme.surfaceVariant),
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
                contentScale = ContentScale.Crop,
            )
            RatingBadge(
                voteAverage = movie.voteAverage,
                modifier = Modifier.align(Alignment.TopStart).padding(CinevoTheme.spacing.xs),
            )
        }
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RatingBadge(
    voteAverage: Double,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = stringResource(R.string.movie_rating, voteAverage),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = CinevoTheme.spacing.xs, vertical = CinevoTheme.spacing.xxs),
        )
    }
}

@Composable
private fun TrendingSkeletonRow(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.trending_loading)
    val pulse by rememberInfiniteTransition(label = "trendingSkeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "trendingSkeletonAlpha",
    )
    LazyRow(
        modifier = modifier.semantics { contentDescription = description }.graphicsLayer { alpha = pulse },
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm),
        userScrollEnabled = false,
    ) {
        items(count = 4) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = modifier.width(PosterWidth),
        verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs),
    ) {
        Box(modifier = Modifier.posterFrame(MaterialTheme.shapes.medium, color))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(color),
        )
    }
}

@Composable
private fun InlineError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) { Text(text = stringResource(R.string.retry)) }
    }
}

/** Full-width, 2:3 poster-shaped area with a background shown until (or instead of) the image. */
private fun Modifier.posterFrame(
    shape: Shape,
    background: Color,
): Modifier = fillMaxWidth().aspectRatio(ratio = 2f / 3f).clip(shape).background(background)
