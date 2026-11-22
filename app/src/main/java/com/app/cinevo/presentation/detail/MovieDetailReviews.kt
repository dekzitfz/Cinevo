package com.app.cinevo.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.app.cinevo.domain.model.Review
import com.app.cinevo.presentation.theme.CinevoTheme

private val AvatarSize = 40.dp

/** Up to the first few reviews, with a way to see all of them. */
@Composable
internal fun ReviewsSection(
    reviews: List<Review>,
    totalReviews: Int,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm)) {
        SectionTitle(text = R.string.reviews_title)
        if (reviews.isEmpty()) {
            Text(
                text = stringResource(R.string.reviews_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            reviews.forEach { review -> key(review.id) { ReviewCard(review = review) } }
            OutlinedButton(onClick = onSeeAllClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.reviews_see_all, totalReviews))
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
) {
    val spacing = CinevoTheme.spacing
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
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
                    Text(
                        text = stringResource(R.string.movie_rating, it),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The author's initials, covered by their avatar once it loads. */
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
