package com.app.cinevo.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.data.remote.dto.MovieDto
import com.app.cinevo.data.remote.dto.PagedResponseDto
import com.app.cinevo.di.NetworkModule
import com.app.cinevo.domain.model.Movie
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

class MoviePagingSourceTest {
    private val json = NetworkModule.provideJson()
    private val requestedPages = mutableListOf<Int>()

    /** A source whose pages are decoded from JSON, as Retrofit would, so malformed bodies fail the same way. */
    private fun source(body: (page: Int) -> String) =
        MoviePagingSource { page ->
            requestedPages += page
            json.decodeFromString<PagedResponseDto<MovieDto>>(body(page))
        }

    private fun failingSource(exception: Exception) = MoviePagingSource { throw exception }

    @Test
    fun `refresh loads page 1 and maps movies`() =
        runTest {
            val result = source { pageJson(it, totalPages = 3, ids = listOf(550, 551)) }.load(refresh())

            assertEquals(
                LoadResult.Page(
                    data = listOf(movie(550), movie(551)),
                    prevKey = null,
                    nextKey = 2,
                ),
                result,
            )
            assertEquals(listOf(1), requestedPages)
        }

    @Test
    fun `append loads the requested page with keys on both sides`() =
        runTest {
            val result =
                source {
                    pageJson(
                        it,
                        totalPages = 3,
                        ids = listOf(10),
                    )
                }.load(append(key = 2)) as LoadResult.Page

            assertEquals(listOf(2), requestedPages)
            assertEquals(1, result.prevKey)
            assertEquals(3, result.nextKey)
        }

    @Test
    fun `last page has no next key`() =
        runTest {
            val result =
                source {
                    pageJson(
                        it,
                        totalPages = 3,
                        ids = listOf(10),
                    )
                }.load(append(key = 3)) as LoadResult.Page

            assertNull(result.nextKey)
        }

    @Test
    fun `single page has no keys`() =
        runTest {
            val result = source { pageJson(it, totalPages = 1, ids = listOf(10)) }.load(refresh()) as LoadResult.Page

            assertNull(result.prevKey)
            assertNull(result.nextKey)
        }

    @Test
    fun `stops at TMDB's 500 page limit even when more pages are reported`() =
        runTest {
            val result =
                source {
                    pageJson(
                        it,
                        totalPages = 41_000,
                        ids = listOf(10),
                    )
                }.load(append(key = 500)) as LoadResult.Page

            assertNull(result.nextKey)
        }

    @Test
    fun `empty results return an empty last page`() =
        runTest {
            val result = source { pageJson(it, totalPages = 0, ids = emptyList()) }.load(refresh())

            assertEquals(LoadResult.Page(data = emptyList<Movie>(), prevKey = null, nextKey = null), result)
        }

    @Test
    fun `movies repeated on a later page are dropped`() =
        runTest {
            val source = source { if (it == 1) pageJson(1, 3, listOf(1, 2)) else pageJson(it, 3, listOf(2, 3)) }

            source.load(refresh())
            val second = source.load(append(key = 2)) as LoadResult.Page

            assertEquals(listOf(movie(3)), second.data)
            assertEquals(3, second.nextKey)
        }

    @Test
    fun `HTTP 401 returns an Http error`() = assertError(ApiError.Http(401), httpException(401))

    @Test
    fun `HTTP 500 returns an Http error`() = assertError(ApiError.Http(500), httpException(500))

    @Test
    fun `no network returns a Network error`() = assertError(ApiError.Network, IOException("offline"))

    @Test
    fun `malformed json returns an Unknown error with the parsing cause`() =
        runTest {
            val result = source { """{"page":1,"results":[{"id":"oops"}]""" }.load(refresh())

            val error = ((result as LoadResult.Error).throwable as ApiException).error
            assertTrue(error is ApiError.Unknown && error.cause is SerializationException)
        }

    @Test
    fun `refresh key is the page closest to the anchor`() {
        val pages =
            listOf(
                LoadResult.Page(data = listOf(movie(1), movie(2)), prevKey = null, nextKey = 2),
                LoadResult.Page(data = listOf(movie(3), movie(4)), prevKey = 1, nextKey = 3),
                LoadResult.Page(data = listOf(movie(5)), prevKey = 2, nextKey = null),
            )
        val source = failingSource(IOException())

        assertEquals(1, source.getRefreshKey(state(pages, anchorPosition = 0)))
        assertEquals(2, source.getRefreshKey(state(pages, anchorPosition = 3)))
        assertEquals(3, source.getRefreshKey(state(pages, anchorPosition = 4)))
    }

    @Test
    fun `refresh key is null without an anchor`() {
        val pages = listOf(LoadResult.Page(data = listOf(movie(1)), prevKey = null, nextKey = 2))

        assertNull(failingSource(IOException()).getRefreshKey(state(pages, anchorPosition = null)))
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

    private fun state(
        pages: List<LoadResult.Page<Int, Movie>>,
        anchorPosition: Int?,
    ) = PagingState(pages, anchorPosition, PagingConfig(pageSize = 20), leadingPlaceholderCount = 0)

    private fun httpException(code: Int) = HttpException(Response.error<Unit>(code, "{}".toResponseBody()))

    private fun movie(id: Int) = Movie(id, "Movie $id", "/$id.jpg", null, 7.5, "2026-01-01")

    private fun pageJson(
        page: Int,
        totalPages: Int,
        ids: List<Int>,
    ): String {
        val results =
            ids.joinToString(",") {
                """{"id":$it,"title":"Movie $it","poster_path":"/$it.jpg",""" +
                    """"vote_average":7.5,"release_date":"2026-01-01"}"""
            }
        return """{"page":$page,"results":[$results],"total_pages":$totalPages,"total_results":${ids.size}}"""
    }
}
