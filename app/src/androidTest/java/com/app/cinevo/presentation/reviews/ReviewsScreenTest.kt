package com.app.cinevo.presentation.reviews

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.cinevo.R
import com.app.cinevo.data.paging.ReviewPagingSource
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.presentation.theme.CinevoTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ReviewsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val longContent = (1..40).joinToString(separator = " ") { "Sentence $it of a long review." }
    private var backClicks = 0

    @Test
    fun success_showsTitleMovieAndCleanedReviews() {
        setContent { page(it, totalPages = 1, review("a", content = "**Loved** it."), review("b")) }

        waitForText("Author a")
        composeRule.onNodeWithText(string(R.string.reviews_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("Loved it.").assertIsDisplayed()
        composeRule.onNodeWithText("Author b").assertIsDisplayed()
    }

    @Test
    fun longReview_expandsAndCollapses() {
        setContent { page(it, totalPages = 1, review("a", content = longContent)) }

        waitForText(string(R.string.reviews_read_more))
        composeRule.onNodeWithText(string(R.string.reviews_read_more)).performClick()

        composeRule.onNodeWithText(string(R.string.reviews_show_less)).assertIsDisplayed().performClick()

        composeRule.onNodeWithText(string(R.string.reviews_read_more)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.reviews_show_less)).assertDoesNotExist()
    }

    @Test
    fun shortReview_hasNoReadMore() {
        setContent { page(it, totalPages = 1, review("a")) }

        waitForText("Author a")
        composeRule.onNodeWithText(string(R.string.reviews_read_more)).assertDoesNotExist()
    }

    @Test
    fun backClick_isReported() {
        setContent { page(it, totalPages = 1, review("a")) }

        composeRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun refreshLoading_showsSkeleton() {
        setContent { awaitCancellation() }

        composeRule.onNodeWithContentDescription(string(R.string.reviews_loading)).assertIsDisplayed()
    }

    @Test
    fun empty_showsNoReviewsWithoutRetry() {
        setContent { page(it, totalPages = 0) }

        waitForText(string(R.string.reviews_empty))
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
    }

    @Test
    fun refreshError_showsMessageAndRetryLoadsReviews() {
        var attempts = 0
        setContent {
            if (attempts++ == 0) throw IOException("offline")
            page(it, totalPages = 1, review("a"))
        }

        waitForText(string(R.string.error_no_connection))
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        waitForText("Author a")
        composeRule.onNodeWithText(string(R.string.error_no_connection)).assertDoesNotExist()
    }

    @Test
    fun appendError_keepsReviewsAndRetryLoadsNextPage() {
        var pageTwoAttempts = 0
        setContent { page ->
            when {
                page == 1 -> page(page, totalPages = 2, review("a"))
                pageTwoAttempts++ == 0 -> throw IOException("offline")
                else -> page(page, totalPages = 2, review("b"))
            }
        }

        waitForText(string(R.string.reviews_load_more_error))
        composeRule.onNodeWithText("Author a").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        waitForText("Author b")
        composeRule.onNodeWithText(string(R.string.reviews_load_more_error)).assertDoesNotExist()
    }

    @Test
    fun expandedReview_staysExpandedAfterStateRestorationWithFreshlyLoadedPages() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // remember (not rememberSaveable): restoration builds a new pager that loads again, as a new ViewModel
            // does after process death. The delay keeps the loading skeleton up for a while, like a real request.
            val reviews =
                remember {
                    pager {
                        delay(NETWORK_DELAY_MS)
                        page(it, totalPages = 1, review("a", content = longContent))
                    }
                }
            CinevoTheme {
                ReviewsScreen(movieTitle = "Fight Club", reviews = reviews.collectAsLazyPagingItems(), onBackClick = {})
            }
        }
        waitForText(string(R.string.reviews_read_more))
        composeRule.onNodeWithText(string(R.string.reviews_read_more)).performClick()
        composeRule.onNodeWithText(string(R.string.reviews_show_less)).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        waitForText(string(R.string.reviews_show_less))
        composeRule.onNodeWithText(string(R.string.reviews_read_more)).assertDoesNotExist()
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
    }

    private fun pager(fetchPage: suspend (page: Int) -> PagedResponseDto<ReviewDto>) =
        Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) { ReviewPagingSource(fetchPage) }.flow

    private fun setContent(fetchPage: suspend (page: Int) -> PagedResponseDto<ReviewDto>) {
        val reviews = pager(fetchPage)
        composeRule.setContent {
            CinevoTheme {
                ReviewsScreen(
                    movieTitle = "Fight Club",
                    reviews = reviews.collectAsLazyPagingItems(),
                    onBackClick = { backClicks++ },
                )
            }
        }
    }

    private fun page(
        page: Int,
        totalPages: Int,
        vararg reviews: ReviewDto,
    ) = PagedResponseDto(results = reviews.toList(), totalPages = totalPages, totalResults = reviews.size)

    private fun review(
        id: String,
        content: String = "Review $id",
    ) = ReviewDto(id = id, author = "Author $id", content = content)

    private fun waitForText(text: String) = composeRule.waitUntilExactlyOneExists(hasText(text))

    private fun string(
        @StringRes id: Int,
    ): String = composeRule.activity.getString(id)
}
