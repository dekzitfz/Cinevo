package com.app.cinevo.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.usecase.GetMovieDetailUseCase
import com.app.cinevo.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// SavedStateHandle.toRoute() decodes route arguments through Bundle, which needs the Android framework.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MovieDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieDetail: GetMovieDetailUseCase = mockk()
    private val movie =
        MovieDetail(550, "Fight Club", null, null, null, null, null, 139, 8.4, 100, emptyList(), null, emptyList(), 0)

    private fun viewModel() =
        MovieDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("movieId" to 550)),
            getMovieDetail = getMovieDetail,
        )

    @Test
    fun `loads the movie from the route`() =
        runTest {
            coEvery { getMovieDetail(550) } returns ApiResult.Success(movie)

            viewModel().uiState.test {
                assertEquals(MovieDetailUiState.Loading, awaitItem())
                assertEquals(MovieDetailUiState.Success(movie), awaitItem())
            }
            coVerify(exactly = 1) { getMovieDetail(550) }
        }

    @Test
    fun `404 shows movie not found without retry`() =
        runTest {
            coEvery { getMovieDetail(550) } returns ApiResult.Error(ApiError.Http(404))

            viewModel().uiState.test {
                skipItems(1)
                assertEquals(MovieDetailUiState.Error(R.string.movie_not_found, canRetry = false), awaitItem())
            }
        }

    @Test
    fun `maps other errors to a retryable message`() =
        runTest {
            val cases =
                listOf(
                    ApiError.Network to R.string.error_no_connection,
                    ApiError.Http(401) to R.string.error_unauthorized,
                    ApiError.Http(500) to R.string.error_server,
                    ApiError.Unknown(IllegalStateException()) to R.string.error_unknown,
                )
            cases.forEach { (error, message) ->
                coEvery { getMovieDetail(550) } returns ApiResult.Error(error)

                viewModel().uiState.test {
                    skipItems(1)
                    assertEquals("for $error", MovieDetailUiState.Error(message, canRetry = true), awaitItem())
                }
            }
        }

    @Test
    fun `retry after an error loads the movie`() =
        runTest {
            coEvery { getMovieDetail(550) } returnsMany
                listOf(ApiResult.Error(ApiError.Network), ApiResult.Success(movie))
            val viewModel = viewModel()

            viewModel.uiState.test {
                skipItems(1)
                assertEquals(MovieDetailUiState.Error(R.string.error_no_connection, canRetry = true), awaitItem())

                viewModel.retry()

                assertEquals(MovieDetailUiState.Loading, awaitItem())
                assertEquals(MovieDetailUiState.Success(movie), awaitItem())
            }
            coVerify(exactly = 2) { getMovieDetail(550) }
        }

    @Test
    fun `retry that fails again shows the error again`() =
        runTest {
            coEvery { getMovieDetail(550) } returns ApiResult.Error(ApiError.Http(500))
            val viewModel = viewModel()
            val error = MovieDetailUiState.Error(R.string.error_server, canRetry = true)

            viewModel.uiState.test {
                skipItems(1)
                assertEquals(error, awaitItem())

                viewModel.retry()

                assertEquals(MovieDetailUiState.Loading, awaitItem())
                assertEquals(error, awaitItem())
            }
        }
}
