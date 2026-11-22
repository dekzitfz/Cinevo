package com.app.cinevo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.network.safeApiCall
import com.app.cinevo.data.mapper.toDomain
import com.app.cinevo.data.paging.MoviePagingSource
import com.app.cinevo.data.paging.ReviewPagingSource
import com.app.cinevo.data.remote.TmdbApi
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.model.MovieDetail
import com.app.cinevo.domain.model.Review
import com.app.cinevo.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepositoryImpl
    @Inject
    constructor(
        private val api: TmdbApi,
    ) : MovieRepository {
        override suspend fun getGenres(): ApiResult<List<Genre>> =
            safeApiCall { api.getMovieGenres().genres.map { it.toDomain() } }

        override suspend fun getTrendingMovies(): ApiResult<List<Movie>> =
            safeApiCall { api.getTrendingMovies().results.map { it.toDomain() } }

        override fun getMoviesByGenre(genreId: Int): Flow<PagingData<Movie>> =
            tmdbPager { MoviePagingSource { page -> api.discoverMovies(genreId = genreId, page = page) } }

        override suspend fun getMovieDetail(movieId: Int): ApiResult<MovieDetail> =
            safeApiCall { api.getMovieDetail(movieId).toDomain() }

        override fun getMovieReviews(movieId: Int): Flow<PagingData<Review>> =
            tmdbPager { ReviewPagingSource { page -> api.getMovieReviews(movieId = movieId, page = page) } }

        private fun <T : Any> tmdbPager(pagingSource: () -> PagingSource<Int, T>): Flow<PagingData<T>> =
            Pager(
                config = PagingConfig(pageSize = TMDB_PAGE_SIZE, enablePlaceholders = false),
                pagingSourceFactory = pagingSource,
            ).flow

        private companion object {
            const val TMDB_PAGE_SIZE = 20
        }
    }
