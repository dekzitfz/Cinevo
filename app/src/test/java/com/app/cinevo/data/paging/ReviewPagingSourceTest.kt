package com.app.cinevo.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.data.remote.dto.ReviewDto
import com.app.cinevo.di.NetworkModule
import com.app.cinevo.domain.model.Review
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant

class ReviewPagingSourceTest {
    private val json = NetworkModule.provideJson()
    private val requestedPages = mutableListOf<Int>()

    /** A source whose pages are decoded from JSON, as Retrofit would, so malformed bodies fail the same way. */
    private fun source(body: (page: Int) -> String) =
        ReviewPagingSource { page ->
            requestedPages += page
            json.decodeFromString<PagedResponseDto<ReviewDto>>(body(page))
        }

    private fun failingSource(exception: Exception) = ReviewPagingSource { throw exception }

    @Test
    fun `refresh loads page 1 and maps reviews`() =
        runTest {
            val result = source { pageJson(it, totalPages = 2, ids = listOf("a", "b")) }.load(refresh())

            assertEquals(LoadResult.Page(data = listOf(review("a"), review("b")), prevKey = null, nextKey = 2), result)
            assertEquals(listOf(1), requestedPages)
        }

    @Test
    fun `append loads the requested page and points to the next one`() =
        runTest {
            val result = source { pageJson(it, totalPages = 3, ids = listOf("c")) }.load(append(key = 2))

            assertEquals(LoadResult.Page(data = listOf(review("c")), prevKey = 1, nextKey = 3), result)
            assertEquals(listOf(2), requestedPages)
        }

    @Test
    fun `last page has no next key`() =
        runTest {
            val result = source { pageJson(it, totalPages = 3, ids = listOf("c")) }.load(append(key = 3))

            assertNull((result as LoadResult.Page).nextKey)
        }

    @Test
    fun `no reviews return an empty last page`() =
        runTest {
            val result = source { pageJson(it, totalPages = 0, ids = emptyList()) }.load(refresh())

            assertEquals(LoadResult.Page(data = emptyList<Review>(), prevKey = null, nextKey = null), result)
        }

    @Test
    fun `reviews repeated on a later page are dropped`() =
        runTest {
            val source = source { if (it == 1) pageJson(1, 2, listOf("a", "b")) else pageJson(it, 2, listOf("b", "c")) }

            source.load(refresh())
            val second = source.load(append(key = 2)) as LoadResult.Page

            assertEquals(listOf(review("c")), second.data)
        }

    @Test
    fun `HTTP 401 returns an Http error`() = assertError(ApiError.Http(401), httpException(401))

    @Test
    fun `HTTP 404 returns an Http error`() = assertError(ApiError.Http(404), httpException(404))

    @Test
    fun `HTTP 500 returns an Http error`() = assertError(ApiError.Http(500), httpException(500))

    @Test
    fun `no network returns a Network error`() = assertError(ApiError.Network, IOException("offline"))

    @Test
    fun `malformed json returns an Unknown error with the parsing cause`() =
        runTest {
            val result = source { """{"page":1,"results":[{"id":"a","author":null}]}""" }.load(refresh())

            val error = ((result as LoadResult.Error).throwable as ApiException).error
            assertTrue(error is ApiError.Unknown && error.cause is SerializationException)
        }

    @Test
    fun `refresh key is the page closest to the anchor`() {
        val pages =
            listOf(
                LoadResult.Page(data = listOf(review("a"), review("b")), prevKey = null, nextKey = 2),
                LoadResult.Page(data = listOf(review("c")), prevKey = 1, nextKey = null),
            )
        val state = PagingState(pages, anchorPosition = 2, PagingConfig(pageSize = 20), leadingPlaceholderCount = 0)

        assertEquals(2, failingSource(IOException()).getRefreshKey(state))
    }

    private fun assertError(
        expected: ApiError,
        thrown: Exception,
    ) = runTest {
        val result = failingSource(thrown).load(refresh())

        assertEquals(expected, ((result as LoadResult.Error).throwable as ApiException).error)
    }

    private fun refresh() = LoadParams.Refresh<Int>(key = null, loadSize = 60, placeholdersEnabled = false)

    private fun append(key: Int) = LoadParams.Append(key = key, loadSize = 20, placeholdersEnabled = false)

    private fun httpException(code: Int) = HttpException(Response.error<Unit>(code, "{}".toResponseBody()))

    private fun review(id: String) =
        Review(
            id = id,
            author = "Author $id",
            avatarUrl = null,
            rating = 7.0,
            content = "Review $id",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        )

    private fun pageJson(
        page: Int,
        totalPages: Int,
        ids: List<String>,
    ): String {
        val results =
            ids.joinToString(",") {
                """{"id":"$it","author":"Author $it","author_details":{"rating":7.0,"avatar_path":null},""" +
                    """"content":"Review $it","created_at":"2024-01-01T00:00:00Z"}"""
            }
        return """{"id":550,"page":$page,"results":[$results],"total_pages":$totalPages,"total_results":${ids.size}}"""
    }
}
