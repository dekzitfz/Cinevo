package com.app.cinevo.core.ui

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.app.cinevo.core.image.TmdbImageUrl
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.presentation.theme.CinevoTheme

/** Poster with rating badge and title below. Fills the width it is given; the poster keeps a 2:3 ratio. */
@Composable
fun MoviePosterCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholder = painterResource(R.drawable.ic_poster_placeholder)
    Column(
        modifier = modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
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

/** Placeholder with the shape of a [MoviePosterCard]. Wrap a group of them in a [SkeletonPulse]. */
@Composable
fun MoviePosterCardSkeleton(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = modifier,
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

/** Pulses the opacity of placeholder [content] and announces it to accessibility services as [contentDescription]. */
@Composable
fun SkeletonPulse(
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Box(
        modifier =
            modifier
                .semantics { this.contentDescription = contentDescription }
                .graphicsLayer { alpha = pulse },
    ) {
        content()
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

/** Full-width, 2:3 poster-shaped area with a background shown until (or instead of) the image. */
private fun Modifier.posterFrame(
    shape: Shape,
    background: Color,
): Modifier = fillMaxWidth().aspectRatio(ratio = 2f / 3f).clip(shape).background(background)

private val PreviewMovie = Movie(1, "A Very Long Title That Wraps Onto Two Lines", null, null, 7.6, null)

@PreviewLightDark
@Composable
private fun MoviePosterCardPreview() {
    CinevoTheme {
        Surface {
            MoviePosterCard(
                movie = PreviewMovie,
                onClick = {},
                modifier = Modifier.width(150.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MoviePosterCardSkeletonPreview() {
    CinevoTheme {
        Surface {
            MoviePosterCardSkeleton(modifier = Modifier.width(150.dp))
        }
    }
}
