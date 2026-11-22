package com.app.cinevo.presentation.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.usecase.GetGenresUseCase
import com.app.cinevo.domain.usecase.GetTrendingMoviesUseCase
import com.app.cinevo.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTrendingMovies: GetTrendingMoviesUseCase = mockk()
    private val getGenres: GetGenresUseCase = mockk()
    private val movies = listOf(Movie(550, "Fight Club", "/poster.jpg", null, 8.4, "1999-10-15"))
    private val genres = listOf(Genre(28, "Action"), Genre(12, "Adventure"))
    private val serverError = SectionUiState.Error(R.string.error_server, ErrorType.Server)
    private val networkError = SectionUiState.Error(R.string.error_no_connection, ErrorType.NoConnection)

    private fun viewModel() = HomeViewModel(getTrendingMovies, getGenres)

    @Test
    fun `starts with both sections loading then shows both on success`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Success(movies)
            coEvery { getGenres() } returns ApiResult.Success(genres)

            viewModel().uiState.test {
                assertEquals(HomeUiState(SectionUiState.Loading, SectionUiState.Loading), awaitItem())
                assertEquals(
                    HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)),
                    awaitLoaded(),
                )
            }
        }

    @Test
    fun `shows Empty for each section that returns no items`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Success(emptyList())
            coEvery { getGenres() } returns ApiResult.Success(emptyList())

            viewModel().uiState.test {
                assertEquals(HomeUiState(SectionUiState.Empty, SectionUiState.Empty), awaitLoaded())
            }
        }

    @Test
    fun `trending failure does not affect genres`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Error(ApiError.Http(500))
            coEvery { getGenres() } returns ApiResult.Success(genres)

            viewModel().uiState.test {
                assertEquals(HomeUiState(serverError, SectionUiState.Success(genres)), awaitLoaded())
            }
        }

    @Test
    fun `genres failure does not affect trending`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Success(movies)
            coEvery { getGenres() } returns ApiResult.Error(ApiError.Network)

            viewModel().uiState.test {
                assertEquals(HomeUiState(SectionUiState.Success(movies), networkError), awaitLoaded())
            }
        }

    @Test
    fun `slow trending does not block genres`() =
        runTest {
            coEvery { getTrendingMovies() } coAnswers { awaitCancellation() }
            coEvery { getGenres() } returns ApiResult.Success(genres)

            viewModel().uiState.test {
                skipItems(1)
                assertEquals(HomeUiState(SectionUiState.Loading, SectionUiState.Success(genres)), awaitItem())
            }
        }

    @Test
    fun `slow genres do not block trending`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Success(movies)
            coEvery { getGenres() } coAnswers { awaitCancellation() }

            viewModel().uiState.test {
                skipItems(1)
                assertEquals(HomeUiState(SectionUiState.Success(movies), SectionUiState.Loading), awaitItem())
            }
        }

    @Test
    fun `retryTrending reloads only trending`() =
        runTest {
            coEvery { getTrendingMovies() } returnsMany
                listOf(ApiResult.Error(ApiError.Http(500)), ApiResult.Success(movies))
            coEvery { getGenres() } returns ApiResult.Success(genres)
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(HomeUiState(serverError, SectionUiState.Success(genres)), awaitLoaded())

                viewModel.retryTrending()

                assertEquals(HomeUiState(SectionUiState.Loading, SectionUiState.Success(genres)), awaitItem())
                assertEquals(HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)), awaitItem())
            }
            coVerify(exactly = 2) { getTrendingMovies() }
            coVerify(exactly = 1) { getGenres() }
        }

    @Test
    fun `retryGenres reloads only genres`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Success(movies)
            coEvery { getGenres() } returnsMany listOf(ApiResult.Error(ApiError.Network), ApiResult.Success(genres))
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(HomeUiState(SectionUiState.Success(movies), networkError), awaitLoaded())

                viewModel.retryGenres()

                assertEquals(HomeUiState(SectionUiState.Success(movies), SectionUiState.Loading), awaitItem())
                assertEquals(HomeUiState(SectionUiState.Success(movies), SectionUiState.Success(genres)), awaitItem())
            }
            coVerify(exactly = 1) { getTrendingMovies() }
            coVerify(exactly = 2) { getGenres() }
        }

    @Test
    fun `retry that fails again shows the error again`() =
        runTest {
            coEvery { getTrendingMovies() } returns ApiResult.Error(ApiError.Http(500))
            coEvery { getGenres() } returns ApiResult.Success(genres)
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(serverError, awaitLoaded().trending)

                viewModel.retryTrending()

                assertEquals(SectionUiState.Loading, awaitItem().trending)
                assertEquals(serverError, awaitItem().trending)
            }
        }

    @Test
    fun `maps each api error to a type and message`() =
        runTest {
            val cases =
                listOf(
                    ApiError.Network to networkError,
                    ApiError.Http(401) to SectionUiState.Error(R.string.error_unauthorized, ErrorType.Unauthorized),
                    ApiError.Http(403) to SectionUiState.Error(R.string.error_unauthorized, ErrorType.Unauthorized),
                    ApiError.Http(500) to serverError,
                    ApiError.Http(503) to serverError,
                    ApiError.Http(404) to SectionUiState.Error(R.string.error_unknown, ErrorType.Unknown),
                    ApiError.Unknown(IOException()) to SectionUiState.Error(R.string.error_unknown, ErrorType.Unknown),
                )
            coEvery { getGenres() } returns ApiResult.Success(genres)
            cases.forEach { (error, expected) ->
                coEvery { getTrendingMovies() } returns ApiResult.Error(error)

                viewModel().uiState.test {
                    assertEquals("for $error", expected, awaitLoaded().trending)
                }
            }
        }

    /** Skips intermediate states until neither section is loading. */
    private suspend fun ReceiveTurbine<HomeUiState>.awaitLoaded(): HomeUiState {
        var state = awaitItem()
        while (state.trending == SectionUiState.Loading || state.genres == SectionUiState.Loading) {
            state = awaitItem()
        }
        return state
    }
}
