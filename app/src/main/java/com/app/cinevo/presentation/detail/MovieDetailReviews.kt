package com.app.cinevo.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.app.cinevo.R
import com.app.cinevo.domain.model.Review
import com.app.cinevo.presentation.reviews.ReviewCard
import com.app.cinevo.presentation.theme.CinevoTheme

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
