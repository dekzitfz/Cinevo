package com.app.cinevo.presentation.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.app.cinevo.domain.model.Review
import com.app.cinevo.presentation.theme.CinevoTheme
import java.time.Instant

private val AvatarSize = 40.dp

/**
 * A review with its author, date and rating. The text is clamped to 4 lines; an [expandable] card clamps it to 6
 * lines instead, with a Read more toggle when it overflows.
 */
@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
) {
    val spacing = CinevoTheme.spacing
    val content = remember(review.content) { cleanReviewContent(review.content) }
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            ReviewHeader(review = review)
            if (expandable) {
                ExpandableReviewText(reviewId = review.id, text = content)
            } else {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Placeholder with the shape of a [ReviewCard]. Wrap a group of them in a `SkeletonPulse`. */
@Composable
fun ReviewCardSkeleton(modifier: Modifier = Modifier) {
    val spacing = CinevoTheme.spacing
    val color = MaterialTheme.colorScheme.surfaceVariant
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(AvatarSize).clip(CircleShape).background(color))
                SkeletonLine(color = color, fraction = 0.4f)
            }
            repeat(times = 3) { SkeletonLine(color = color, fraction = 1f) }
            SkeletonLine(color = color, fraction = 0.6f)
        }
    }
}

@Composable
private fun ReviewHeader(
    review: Review,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = review.author, url = review.avatarUrl)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = review.author,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            review.createdAt?.let {
                Text(
                    text = formatDate(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        review.rating?.let {
            val description = stringResource(R.string.rating_description, it)
            Text(
                text = stringResource(R.string.movie_rating, it),
                modifier = Modifier.semantics { contentDescription = description },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** Text clamped to 6 lines until expanded. The expanded state is saved per review, so it survives scrolling. */
@Composable
private fun ExpandableReviewText(
    reviewId: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(reviewId) { mutableStateOf(false) }
    var overflows by remember(text) { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else 6,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
        )
        if (expanded || overflows) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(text = stringResource(if (expanded) R.string.reviews_show_less else R.string.reviews_read_more))
            }
        }
    }
}

/** The author's initials, covered by their avatar once it loads. Silent for screen readers: the name follows. */
@Composable
private fun Avatar(
    name: String,
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(AvatarSize).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            modifier = Modifier.semantics { hideFromAccessibility() },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun SkeletonLine(
    color: Color,
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth(fraction)
                .height(14.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color),
    )
}

private val PreviewReview =
    Review(
        id = "r1",
        author = "Jamie Rivera",
        avatarUrl = null,
        rating = 8.0,
        content =
            "**Tense** from the first frame to the last.\n\n\n\nThe sound design alone makes this worth seeing on " +
                "the biggest screen you can find, and the final act lands every beat it set up. ".repeat(n = 3),
        createdAt = Instant.parse("2026-10-05T12:00:00Z"),
    )

@PreviewLightDark
@Composable
private fun ReviewCardPreview() {
    CinevoTheme {
        Surface {
            ReviewCard(review = PreviewReview, modifier = Modifier.padding(CinevoTheme.spacing.md))
        }
    }
}

@PreviewLightDark
@Composable
private fun ReviewCardExpandablePreview() {
    CinevoTheme {
        Surface {
            ReviewCard(review = PreviewReview, expandable = true, modifier = Modifier.padding(CinevoTheme.spacing.md))
        }
    }
}

@PreviewLightDark
@Composable
private fun ReviewCardSkeletonPreview() {
    CinevoTheme {
        Surface {
            ReviewCardSkeleton(modifier = Modifier.padding(CinevoTheme.spacing.md))
        }
    }
}
