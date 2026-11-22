package com.app.cinevo.presentation.genres

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.cinevo.R
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.presentation.theme.CinevoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var retryClicks = 0
    private var clickedGenre: Pair<Int, String>? = null

    @Test
    fun loading_showsProgressIndicator() {
        setContent(GenreUiState.Loading)

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.app_name)).assertIsDisplayed()
    }

    @Test
    fun success_showsGenresAndReportsClicks() {
        setContent(GenreUiState.Success(listOf(Genre(28, "Action"), Genre(35, "Comedy"))))

        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText("Comedy").assertIsDisplayed().performClick()

        assertEquals(35 to "Comedy", clickedGenre)
    }

    @Test
    fun empty_showsEmptyMessageWithoutRetry() {
        setContent(GenreUiState.Empty)

        composeRule.onNodeWithText(string(R.string.genres_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).assertDoesNotExist()
    }

    @Test
    fun error_showsMessageAndRetryInvokesCallback() {
        setContent(GenreUiState.Error(R.string.error_no_connection, ErrorType.NoConnection))

        composeRule.onNodeWithText(string(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(1, retryClicks)
    }

    private fun setContent(uiState: GenreUiState) {
        composeRule.setContent {
            CinevoTheme {
                GenreScreen(
                    uiState = uiState,
                    onGenreClick = { id, name -> clickedGenre = id to name },
                    onRetry = { retryClicks++ },
                )
            }
        }
    }

    private fun string(
        @StringRes id: Int,
    ): String = composeRule.activity.getString(id)
}
