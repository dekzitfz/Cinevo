package com.app.cinevo.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.app.cinevo.core.image.TmdbImageUrl
import com.app.cinevo.core.ui.EmptyView
import com.app.cinevo.core.ui.ErrorView
import com.app.cinevo.core.ui.LoadingView
import com.app.cinevo.core.ui.SectionTitle
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.model.Video
import com.app.cinevo.presentation.theme.CinevoTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate

private val BackdropHeight = 280.dp
private val PosterWidth = 120.dp

/** How far the poster's top sits above the bottom of the backdrop. */
private val PosterOverlap = 96.dp

@Composable
fun MovieDetailScreen(
    onBackClick: () -> Unit,
    onSeeAllReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovieDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onSeeAllReviewsClick = onSeeAllReviewsClick,
        modifier = modifier,
    )
}

/** [trailerPlayer] draws the player for a YouTube video key into a 16:9 frame; tests and previews swap it out. */
@Composable
fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onSeeAllReviewsClick: (movieId: Int, movieTitle: String) -> Unit,
    modifier: Modifier = Modifier,
    trailerPlayer: @Composable (videoKey: String) -> Unit = { YouTubeTrailerPlayer(videoKey = it) },
) {
    // Hoisted above the state switch so the scroll position is restored even when the screen comes back from
    // process death through Loading before the content reappears.
    val scrollState = rememberScrollState()
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
            val statusModifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            when (uiState) {
                MovieDetailUiState.Loading -> {
                    LoadingView(modifier = statusModifier)
                }

                is MovieDetailUiState.Error -> {
                    val message = stringResource(uiState.message)
                    if (uiState.canRetry) {
                        ErrorView(message = message, onRetry = onRetry, modifier = statusModifier)
                    } else {
                        EmptyView(message = message, modifier = statusModifier)
                    }
                }

                is MovieDetailUiState.Success -> {
                    val movie = uiState.movie
                    MovieDetailContent(
                        movie = movie,
                        scrollState = scrollState,
                        onSeeAllReviewsClick = { onSeeAllReviewsClick(movie.id, movie.title) },
                        trailerPlayer = trailerPlayer,
                    )
                }
            }
            BackButton(
                onClick = onBackClick,
                modifier =
                    Modifier
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start),
                        ).padding(CinevoTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetail,
    scrollState: ScrollState,
    onSeeAllReviewsClick: () -> Unit,
    trailerPlayer: @Composable (videoKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = CinevoTheme.spacing
    val horizontalInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(bottom = spacing.lg),
    ) {
        DetailHeader(movie = movie, modifier = Modifier.windowInsetsPadding(horizontalInsets))
        Column(
            modifier = Modifier.windowInsetsPadding(horizontalInsets).padding(horizontal = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (movie.genres.isNotEmpty()) {
                GenreChips(
                    genres = movie.genres,
                    modifier = Modifier.padding(top = spacing.md),
                )
            }
            movie.tagline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            movie.overview?.let { Text(text = it, style = MaterialTheme.typography.bodyLarge) }
            TrailerSection(trailer = movie.trailer, trailerPlayer = trailerPlayer)
            ReviewsSection(
                reviews = movie.reviews,
                totalReviews = movie.totalReviews,
                onSeeAllClick = onSeeAllReviewsClick,
            )
        }
    }
}

/** Backdrop fading into the background, with the poster overlapping its bottom edge next to the key facts. */
@Composable
private fun DetailHeader(
    movie: MovieDetail,
    modifier: Modifier = Modifier,
) {
    val spacing = CinevoTheme.spacing
    val colors = MaterialTheme.colorScheme
    // Clear over the top half, then fading into the background.
    val fade = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, colors.background))
    Box(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = TmdbImageUrl.backdrop(movie.backdropPath),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(BackdropHeight)
                    .background(colors.surfaceVariant)
                    .drawWithContent {
                        drawContent()
                        drawRect(fade)
                    },
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier.padding(start = spacing.md, end = spacing.md, top = BackdropHeight - PosterOverlap),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.Bottom,
        ) {
            val placeholder = painterResource(R.drawable.ic_poster_placeholder)
            AsyncImage(
                model = TmdbImageUrl.poster(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_description, movie.title),
                modifier =
                    Modifier
                        .width(PosterWidth)
                        .aspectRatio(ratio = 2f / 3f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.surfaceVariant),
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs),
            ) {
                Text(
                    text = movie.title,
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                )
                Facts(year = movie.releaseDate?.year, runtimeMinutes = movie.runtimeMinutes)
                Rating(voteAverage = movie.voteAverage, voteCount = movie.voteCount)
            }
        }
    }
}

/** Release year and runtime, such as "1999 · 2h 19m". Draws nothing when both are unknown. */
@Composable
private fun Facts(
    year: Int?,
    runtimeMinutes: Int?,
    modifier: Modifier = Modifier,
) {
    val runtime = runtimeMinutes?.let(::runtimeOf)
    val yearText = year?.toString()
    val facts = listOfNotNull(yearText, runtime?.let { runtimeText(it) })
    if (facts.isEmpty()) return
    val description = listOfNotNull(yearText, runtime?.let { runtimeDescription(it) })
    val descriptionSeparator = stringResource(R.string.movie_facts_description_separator)
    Text(
        text = facts.joinToString(separator = stringResource(R.string.movie_facts_separator)),
        modifier =
            modifier.semantics { contentDescription = description.joinToString(separator = descriptionSeparator) },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Average rating and vote count, read aloud as one phrase such as "Rated 8.4 out of 10 from 1,234 votes". */
@Composable
private fun Rating(
    voteAverage: Double,
    voteCount: Int,
    modifier: Modifier = Modifier,
) {
    val formattedCount = NumberFormat.getIntegerInstance().format(voteCount)
    val description =
        pluralStringResource(R.plurals.movie_rating_description, voteCount, voteAverage, formattedCount)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.movie_rating, voteAverage),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = pluralStringResource(R.plurals.movie_vote_count, voteCount, formattedCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun GenreChips(
    genres: List<Genre>,
    modifier: Modifier = Modifier,
) {
    val spacing = CinevoTheme.spacing
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        genres.forEach { genre ->
            Surface(
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun TrailerSection(
    trailer: Video?,
    trailerPlayer: @Composable (videoKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs)) {
        SectionTitle(text = R.string.trailer_title)
        if (trailer == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = stringResource(R.string.trailer_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(CinevoTheme.spacing.md),
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = 16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Black),
            ) {
                trailerPlayer(trailer.key)
            }
            val context = LocalContext.current
            TextButton(
                onClick = { context.openYouTubeVideo(trailer.key) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(R.string.trailer_open_in_youtube))
            }
        }
    }
}

/** Stays readable over both the backdrop and the plain background. */
@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.navigate_up),
        )
    }
}

private val PreviewMovie =
    MovieDetail(
        id = 1,
        title = "The Long Night",
        tagline = "Some nights never end.",
        overview =
            "A lighthouse keeper on a remote island must survive the longest night of the year while a storm " +
                "cuts her off from the mainland and something stirs in the water below.",
        posterPath = null,
        backdropPath = null,
        releaseDate = LocalDate.of(2026, 10, 2),
        runtimeMinutes = 135,
        voteAverage = 8.4,
        voteCount = 12_345,
        genres = listOf(Genre(53, "Thriller"), Genre(18, "Drama"), Genre(9648, "Mystery")),
        trailer = Video("abc123", "YouTube", "Trailer", true, null),
        reviews =
            listOf(
                Review(
                    id = "r1",
                    author = "Jamie Rivera",
                    avatarUrl = null,
                    rating = 9.0,
                    content =
                        "Tense from the first frame to the last. The sound design alone makes this worth seeing " +
                            "on the biggest screen you can find, and the final act lands every beat it set up.",
                    createdAt = Instant.parse("2026-10-05T12:00:00Z"),
                ),
                Review("r2", "moviebuff", null, null, "Slow start, great payoff.", null),
            ),
        totalReviews = 24,
    )

@Composable
private fun MovieDetailScreenPreview(uiState: MovieDetailUiState) {
    CinevoTheme {
        MovieDetailScreen(
            uiState = uiState,
            onBackClick = {},
            onRetry = {},
            onSeeAllReviewsClick = { _, _ -> },
            trailerPlayer = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailScreenSuccessPreview() {
    MovieDetailScreenPreview(MovieDetailUiState.Success(PreviewMovie))
}

@PreviewLightDark
@Composable
private fun MovieDetailScreenNoTrailerNoReviewsPreview() {
    MovieDetailScreenPreview(
        MovieDetailUiState.Success(PreviewMovie.copy(trailer = null, reviews = emptyList(), totalReviews = 0)),
    )
}

@PreviewLightDark
@Composable
private fun MovieDetailScreenErrorPreview() {
    MovieDetailScreenPreview(MovieDetailUiState.Error(R.string.error_no_connection, canRetry = true))
}
