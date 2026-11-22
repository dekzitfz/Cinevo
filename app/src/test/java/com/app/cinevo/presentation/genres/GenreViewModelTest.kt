package com.app.cinevo.presentation.genres

import app.cash.turbine.test
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.ui.ErrorType
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.usecase.GetGenresUseCase
import com.app.cinevo.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class GenreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getGenres: GetGenresUseCase = mockk()
    private val genres = listOf(Genre(28, "Action"), Genre(12, "Adventure"))

    @Test
    fun `emits Loading then Success when genres load`() =
        runTest {
            coEvery { getGenres() } returns ApiResult.Success(genres)

            GenreViewModel(getGenres).uiState.test {
                assertEquals(GenreUiState.Loading, awaitItem())
                assertEquals(GenreUiState.Success(genres), awaitItem())
            }
        }

    @Test
    fun `emits Loading then Empty when no genres are returned`() =
        runTest {
            coEvery { getGenres() } returns ApiResult.Success(emptyList())

            GenreViewModel(getGenres).uiState.test {
                assertEquals(GenreUiState.Loading, awaitItem())
                assertEquals(GenreUiState.Empty, awaitItem())
            }
        }

    @Test
    fun `emits Loading then Error when the request fails`() =
        runTest {
            coEvery { getGenres() } returns ApiResult.Error(ApiError.Network)

            GenreViewModel(getGenres).uiState.test {
                assertEquals(GenreUiState.Loading, awaitItem())
                assertEquals(GenreUiState.Error(R.string.error_no_connection, ErrorType.NoConnection), awaitItem())
            }
        }

    @Test
    fun `maps each api error to a type and message`() =
        runTest {
            val cases =
                listOf(
                    ApiError.Network to GenreUiState.Error(R.string.error_no_connection, ErrorType.NoConnection),
                    ApiError.Http(401) to GenreUiState.Error(R.string.error_unauthorized, ErrorType.Unauthorized),
                    ApiError.Http(403) to GenreUiState.Error(R.string.error_unauthorized, ErrorType.Unauthorized),
                    ApiError.Http(500) to GenreUiState.Error(R.string.error_server, ErrorType.Server),
                    ApiError.Http(503) to GenreUiState.Error(R.string.error_server, ErrorType.Server),
                    ApiError.Http(404) to GenreUiState.Error(R.string.error_unknown, ErrorType.Unknown),
                    ApiError.Unknown(IOException()) to GenreUiState.Error(R.string.error_unknown, ErrorType.Unknown),
                )
            cases.forEach { (error, expected) ->
                coEvery { getGenres() } returns ApiResult.Error(error)

                GenreViewModel(getGenres).uiState.test {
                    skipItems(1)
                    assertEquals("for $error", expected, awaitItem())
                }
            }
        }

    @Test
    fun `retry after error emits Loading then Success`() =
        runTest {
            coEvery { getGenres() } returnsMany listOf(ApiResult.Error(ApiError.Http(500)), ApiResult.Success(genres))
            val viewModel = GenreViewModel(getGenres)

            viewModel.uiState.test {
                assertEquals(GenreUiState.Loading, awaitItem())
                assertEquals(GenreUiState.Error(R.string.error_server, ErrorType.Server), awaitItem())

                viewModel.retry()

                assertEquals(GenreUiState.Loading, awaitItem())
                assertEquals(GenreUiState.Success(genres), awaitItem())
            }
            coVerify(exactly = 2) { getGenres() }
        }
}
