package com.app.cinevo.presentation.movies

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.cinevo.R
import com.app.cinevo.data.paging.MoviePagingSource
import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.presentation.theme.CinevoTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MovieListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var clickedMovieId: Int? = null
    private var backClicks = 0

    @Test
    fun success_showsGenreTitleAndMovies() {
        setContent { page(it, totalPages = 1, 550, 603) }

        waitForText("Movie 550")
        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText("Movie 603").assertIsDisplayed()
    }

    @Test
    fun movieClick_reportsMovieId() {
        setContent { page(it, totalPages = 1, 550, 603) }

        waitForText("Movie 603")
        composeRule.onNodeWithText("Movie 603").performClick()

        assertEquals(603, clickedMovieId)
    }

    @Test
    fun backClick_isReported() {
        setContent { page(it, totalPages = 1, 550) }

        composeRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun refreshLoading_showsSkeleton() {
        setContent { awaitCancellation() }

        composeRule.onNodeWithContentDescription(string(R.string.movies_loading)).assertIsDisplayed()
    }

    @Test
    fun refreshError_showsMessageAndRetryLoadsMovies() {
        var attempts = 0
        setContent {
            if (attempts++ == 0) throw IOException("offline")
            page(it, totalPages = 1, 550)
        }

        waitForText(string(R.string.error_no_connection))
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        waitForText("Movie 550")
        composeRule.onNodeWithText(string(R.string.error_no_connection)).assertDoesNotExist()
    }

    @Test
    fun refreshUnauthorized_showsUnauthorizedMessage() {
        setContent { throw HttpException(Response.error<Unit>(401, "{}".toResponseBody())) }

        waitForText(string(R.string.error_unauthorized))
        composeRule.onNodeWithText(string(R.string.retry)).assertIsDisplayed()
    }

    @Test
    fun empty_showsEmptyMessageWithoutRetry() {
        setContent { page(it, totalPages = 0) }

        waitForText(string(R.string.movies_empty))
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
    }

    @Test
    fun appendError_keepsMoviesAndRetryLoadsNextPage() {
        var pageTwoAttempts = 0
        setContent { page ->
            when {
                page == 1 -> page(page, totalPages = 2, 550, 603)
                pageTwoAttempts++ == 0 -> throw IOException("offline")
                else -> page(page, totalPages = 2, 680)
            }
        }

        waitForText(string(R.string.movies_load_more_error))
        composeRule.onNodeWithText("Movie 550").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        waitForText("Movie 680")
        composeRule.onNodeWithText(string(R.string.movies_load_more_error)).assertDoesNotExist()
    }

    @Test
    fun scrollPosition_survivesStateRestorationWithFreshlyLoadedPages() {
        val ids = (1..40).toList().toIntArray()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // remember (not rememberSaveable): restoration builds a new pager that loads again, as a new ViewModel
            // does after process death. The delay keeps the loading skeleton up for a while, like a real request.
            val movies =
                remember {
                    pager {
                        delay(NETWORK_DELAY_MS)
                        page(it, totalPages = 1, *ids)
                    }
                }
            CinevoTheme {
                MovieListScreen(
                    genreName = "Action",
                    movies = movies.collectAsLazyPagingItems(),
                    onMovieClick = {},
                    onBackClick = {},
                )
            }
        }
        waitForText("Movie 1")
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(30)
        composeRule.onNodeWithText("Movie 31").assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        waitForText("Movie 31")
        composeRule.onNodeWithText("Movie 31").assertIsDisplayed()
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
    }

    private fun pager(fetchPage: suspend (page: Int) -> PagedResponseDto<MovieDto>) =
        Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) { MoviePagingSource(fetchPage) }.flow

    private fun setContent(fetchPage: suspend (page: Int) -> PagedResponseDto<MovieDto>) {
        val movies = pager(fetchPage)
        composeRule.setContent {
            CinevoTheme {
                MovieListScreen(
                    genreName = "Action",
                    movies = movies.collectAsLazyPagingItems(),
                    onMovieClick = { clickedMovieId = it },
                    onBackClick = { backClicks++ },
                )
            }
        }
    }

    private fun page(
        page: Int,
        totalPages: Int,
        vararg ids: Int,
    ) = PagedResponseDto(
        results = ids.map { MovieDto(id = it, title = "Movie $it") },
        totalPages = totalPages,
        totalResults = ids.size,
    )

    private fun waitForText(text: String) = composeRule.waitUntilExactlyOneExists(hasText(text))

    private fun string(
        @StringRes id: Int,
    ): String = composeRule.activity.getString(id)
}
