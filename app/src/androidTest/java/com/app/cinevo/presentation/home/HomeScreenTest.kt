package com.app.cinevo.presentation.home

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.cinevo.R
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.presentation.theme.CinevoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val movies =
        listOf(
            Movie(550, "Fight Club", null, 8.4),
            Movie(603, "The Matrix", null, 8.2),
        )
    private val genres = listOf(Genre(28, "Action"), Genre(35, "Comedy"))
    private val networkError = SectionUiState.Error(R.string.error_no_connection, ErrorType.NoConnection)
    private val serverError = SectionUiState.Error(R.string.error_server, ErrorType.Server)

    private var trendingRetries = 0
    private var genreRetries = 0
    private var clickedMovieId: Int? = null
    private var clickedGenre: Pair<Int, String>? = null

    @Test
    fun loading_showsTrendingSkeletonAndGenresProgress() {
        setContent(HomeUiState())

        composeRule.onNodeWithText(string(R.string.app_name)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.trending_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.trending_loading)).assertIsDisplayed()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun success_showsBothSectionsAndRatings() {
        setContent(HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)))

        composeRule.onNodeWithText(string(R.string.trending_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                composeRule.activity.getString(R.string.movie_card_description, "Fight Club", 8.4),
            ).assertIsDisplayed()
            .assertHasClickAction()
        composeRule.onNodeWithText(string(R.string.genres_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun success_reportsMovieClick() {
        setContent(HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)))

        composeRule.onNodeWithText("The Matrix").performClick()

        assertEquals(603, clickedMovieId)
    }

    @Test
    fun success_reportsGenreClick() {
        setContent(HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)))

        composeRule.onNodeWithText("Comedy").performClick()

        assertEquals(35 to "Comedy", clickedGenre)
    }

    @Test
    fun trendingError_showsInlineRetryWhileGenresStayVisible() {
        setContent(HomeUiState(networkError, SectionUiState.Success(genres)))

        composeRule.onNodeWithText(string(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(1, trendingRetries)
        assertEquals(0, genreRetries)
    }

    @Test
    fun genresError_showsRetryWhileTrendingStaysVisible() {
        setContent(HomeUiState(SectionUiState.Success(movies), serverError))

        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.error_server)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(0, trendingRetries)
        assertEquals(1, genreRetries)
    }

    @Test
    fun trendingEmpty_hidesTrendingSection() {
        setContent(HomeUiState(SectionUiState.Empty, SectionUiState.Success(genres)))

        composeRule.onNodeWithText(string(R.string.trending_title)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun genresEmpty_showsEmptyMessageWithoutRetry() {
        setContent(HomeUiState(SectionUiState.Success(movies), SectionUiState.Empty))

        composeRule.onNodeWithText(string(R.string.genres_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
    }

    private fun setContent(uiState: HomeUiState) {
        composeRule.setContent {
            CinevoTheme {
                HomeScreen(
                    uiState = uiState,
                    onMovieClick = { clickedMovieId = it },
                    onGenreClick = { id, name -> clickedGenre = id to name },
                    onRetryTrending = { trendingRetries++ },
                    onRetryGenres = { genreRetries++ },
                )
            }
        }
    }

    private fun string(
        @StringRes id: Int,
    ): String = composeRule.activity.getString(id)
}
