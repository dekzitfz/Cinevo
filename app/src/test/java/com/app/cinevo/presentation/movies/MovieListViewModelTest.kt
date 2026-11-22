package com.app.cinevo.presentation.movies

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.data.paging.MoviePagingSource
import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.data.remote.dto.PagedMovieResponseDto
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.usecase.GetMoviesByGenreUseCase
import com.app.cinevo.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

// SavedStateHandle.toRoute() decodes route arguments through Bundle, which needs the Android framework.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MovieListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMoviesByGenre: GetMoviesByGenreUseCase = mockk()
    private val movies =
        listOf(
            Movie(550, "Fight Club", "/poster.jpg", null, 8.4, "1999-10-15"),
            Movie(603, "The Matrix", null, null, 8.2, "1999-03-31"),
        )
    private val page =
        PagedMovieResponseDto(
            page = 1,
            results =
                movies.map {
                    MovieDto(
                        it.id,
                        it.title,
                        it.posterPath,
                        it.backdropPath,
                        it.voteAverage,
                        it.releaseDate,
                    )
                },
            totalPages = 1,
            totalResults = movies.size,
        )

    private fun pagerOf(source: MoviePagingSource) = Pager(PagingConfig(pageSize = 20)) { source }.flow

    private fun viewModel() =
        MovieListViewModel(
            savedStateHandle = SavedStateHandle(mapOf("genreId" to 28, "genreName" to "Action")),
            getMoviesByGenre = getMoviesByGenre,
        )

    @Test
    fun `reads the genre from the route and loads its movies`() =
        runTest {
            every { getMoviesByGenre(28) } returns pagerOf(MoviePagingSource { page })

            val viewModel = viewModel()

            assertEquals("Action", viewModel.genreName)
            assertEquals(movies, viewModel.movies.asSnapshot())
            verify(exactly = 1) { getMoviesByGenre(28) }
        }

    @Test
    fun `failed load surfaces the typed api error`() =
        runTest {
            every { getMoviesByGenre(28) } returns pagerOf(MoviePagingSource { throw IOException("offline") })

            val error = runCatching { viewModel().movies.asSnapshot() }.exceptionOrNull()

            assertEquals(ApiError.Network, (error as ApiException).error)
        }
}
