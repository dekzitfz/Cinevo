package com.app.cinevo.presentation.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.data.paging.ReviewPagingSource
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.usecase.GetMovieReviewsUseCase
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
class ReviewsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieReviews: GetMovieReviewsUseCase = mockk()

    private fun pagerOf(source: ReviewPagingSource) = Pager(PagingConfig(pageSize = 20)) { source }.flow

    private fun viewModel() =
        ReviewsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("movieId" to 550, "movieTitle" to "Fight Club")),
            getMovieReviews = getMovieReviews,
        )

    @Test
    fun `reads the movie from the route and loads its reviews`() =
        runTest {
            val page =
                PagedResponseDto(
                    results =
                        listOf(
                            ReviewDto("r1", "Jane", content = "Great."),
                            ReviewDto("r2", "Sam", content = "Meh."),
                        ),
                    totalPages = 1,
                )
            every { getMovieReviews(550) } returns pagerOf(ReviewPagingSource { page })

            val viewModel = viewModel()

            assertEquals("Fight Club", viewModel.movieTitle)
            assertEquals(
                listOf(
                    Review("r1", "Jane", null, null, "Great.", null),
                    Review("r2", "Sam", null, null, "Meh.", null),
                ),
                viewModel.reviews.asSnapshot(),
            )
            verify(exactly = 1) { getMovieReviews(550) }
        }

    @Test
    fun `failed load surfaces the typed api error`() =
        runTest {
            every { getMovieReviews(550) } returns pagerOf(ReviewPagingSource { throw IOException("offline") })

            val error = runCatching { viewModel().reviews.asSnapshot() }.exceptionOrNull()

            assertEquals(ApiError.Network, (error as ApiException).error)
        }
}
