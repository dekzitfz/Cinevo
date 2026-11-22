package com.app.cinevo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.app.cinevo.core.network.ApiException
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.network.safeApiCall
import com.app.cinevo.data.remote.dto.PagedResponseDto
import java.util.concurrent.ConcurrentHashMap

/**
 * Pages through a TMDB list endpoint. [fetchPage] receives 1-based page numbers, [map] turns each result into a
 * domain value and [idOf] identifies results so a repeated one is shown only once. Failures carry an [ApiException].
 */
open class TmdbPagingSource<Dto, Value : Any>(
    private val fetchPage: suspend (page: Int) -> PagedResponseDto<Dto>,
    private val idOf: (Dto) -> Any,
    private val map: (Dto) -> Value,
) : PagingSource<Int, Value>() {
    // TMDB can repeat a result on a later page when the ordering shifts between requests. Duplicate ids would crash
    // lazy lists keyed by id, so each generation shows a result only once. Appends and prepends may run concurrently.
    private val seenIds = ConcurrentHashMap.newKeySet<Any>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        val page = params.key ?: FIRST_PAGE
        return when (val result = safeApiCall { fetchPage(page) }) {
            is ApiResult.Success -> {
                val response = result.data
                val lastPage = minOf(response.totalPages, MAX_PAGE)
                LoadResult.Page(
                    data = response.results.filter { seenIds.add(idOf(it)) }.map(map),
                    prevKey = if (page == FIRST_PAGE) null else page - 1,
                    nextKey = if (response.results.isEmpty() || page >= lastPage) null else page + 1,
                )
            }

            is ApiResult.Error -> {
                LoadResult.Error(ApiException(result.error))
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Value>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.let { page -> page.prevKey?.plus(1) ?: page.nextKey?.minus(1) }
        }

    private companion object {
        const val FIRST_PAGE = 1

        /** TMDB rejects page numbers above 500, whatever `total_pages` says. */
        const val MAX_PAGE = 500
    }
}
