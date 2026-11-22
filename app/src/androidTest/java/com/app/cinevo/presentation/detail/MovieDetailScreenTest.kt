package com.app.cinevo.presentation.detail

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.cinevo.R
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.model.Video
import com.app.cinevo.presentation.reviews.formatDate
import com.app.cinevo.presentation.theme.CinevoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MovieDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val reviewDate = Instant.parse("2020-05-01T12:00:00Z")
    private val overview = "An insomniac office worker forms an underground fight club."
    private val movie =
        MovieDetail(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = overview,
            posterPath = null,
            backdropPath = null,
            releaseDate = LocalDate.of(1999, 10, 15),
            runtimeMinutes = 139,
            voteAverage = 8.4,
            voteCount = 1,
            genres = listOf(Genre(18, "Drama"), Genre(53, "Thriller")),
            trailer = Video("yt123", "Official Trailer", "YouTube", "Trailer", true, null),
            reviews =
                listOf(
                    Review("r1", "Jane Doe", null, 9.0, "Loved every minute.", reviewDate),
                    Review("r2", "moviebuff", null, null, "Not for me.", null),
                ),
            totalReviews = 12,
        )

    private var retries = 0
    private var backClicks = 0
    private var seeAllReviews: Pair<Int, String>? = null

    @Test
    fun success_showsDetailsTrailerAndReviews() {
        setContent(MovieDetailUiState.Success(movie))

        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("1999 · 2h 19m").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.movie_rating, 8.4)).assertIsDisplayed()
        composeRule.onNodeWithText("1 vote").assertIsDisplayed()
        composeRule.onNodeWithText("Drama").assertIsDisplayed()
        composeRule.onNodeWithText("Mischief. Mayhem. Soap.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(overview).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Fake player yt123").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.trailer_open_in_youtube)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Jane Doe").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("JD").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(formatDate(reviewDate)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.movie_rating, 9.0)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("moviebuff").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.trailer_unavailable)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.reviews_empty)).assertDoesNotExist()
    }

    @Test
    fun seeAllReviews_reportsMovieIdAndTitle() {
        setContent(MovieDetailUiState.Success(movie))

        composeRule.onNodeWithText(string(R.string.reviews_see_all, 12)).performScrollTo().performClick()

        assertEquals(550 to "Fight Club", seeAllReviews)
    }

    @Test
    fun noTrailer_showsUnavailableStateWithoutPlayer() {
        setContent(MovieDetailUiState.Success(movie.copy(trailer = null)))

        composeRule.onNodeWithText(string(R.string.trailer_unavailable)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Fake player yt123").assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.trailer_open_in_youtube)).assertDoesNotExist()
    }

    @Test
    fun noReviews_showsEmptyMessageWithoutSeeAll() {
        setContent(MovieDetailUiState.Success(movie.copy(reviews = emptyList(), totalReviews = 0)))

        composeRule.onNodeWithText(string(R.string.reviews_empty)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.reviews_see_all, 0)).assertDoesNotExist()
    }

    @Test
    fun error_showsMessageAndRetry() {
        setContent(MovieDetailUiState.Error(R.string.error_no_connection, canRetry = true))

        composeRule.onNodeWithText(string(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun notFound_showsMessageWithoutRetry() {
        setContent(MovieDetailUiState.Error(R.string.movie_not_found, canRetry = false))

        composeRule.onNodeWithText(string(R.string.movie_not_found)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(string(R.string.navigate_up)).assertIsDisplayed()
    }

    @Test
    fun backClick_isReported() {
        setContent(MovieDetailUiState.Success(movie))

        composeRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertEquals(1, backClicks)
    }

    private fun setContent(uiState: MovieDetailUiState) {
        composeRule.setContent {
            CinevoTheme {
                MovieDetailScreen(
                    uiState = uiState,
                    onBackClick = { backClicks++ },
                    onRetry = { retries++ },
                    onSeeAllReviewsClick = { id, title -> seeAllReviews = id to title },
                    trailerPlayer = { Text(text = "Fake player $it") },
                )
            }
        }
    }

    private fun string(
        @StringRes id: Int,
        vararg args: Any,
    ): String = composeRule.activity.getString(id, *args)
}
