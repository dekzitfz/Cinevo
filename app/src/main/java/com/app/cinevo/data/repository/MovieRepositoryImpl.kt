package com.app.cinevo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.core.network.safeApiCall
import com.app.cinevo.data.mapper.toDomain
import com.app.cinevo.data.paging.MoviePagingSource
import com.app.cinevo.data.remote.TmdbApi
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
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
            Pager(config = PagingConfig(pageSize = TMDB_PAGE_SIZE, enablePlaceholders = false)) {
                MoviePagingSource { page -> api.discoverMovies(genreId = genreId, page = page) }
            }.flow

        private companion object {
            const val TMDB_PAGE_SIZE = 20
        }
    }
