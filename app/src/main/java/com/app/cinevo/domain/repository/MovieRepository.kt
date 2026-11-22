package com.app.cinevo.domain.repository

import androidx.paging.PagingData
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getGenres(): ApiResult<List<Genre>>

    suspend fun getTrendingMovies(): ApiResult<List<Movie>>

    /** Movies in a genre, most popular first. Failed loads carry an `ApiException`. */
    fun getMoviesByGenre(genreId: Int): Flow<PagingData<Movie>>

    /** Details with the trailer and the first reviews. */
    suspend fun getMovieDetail(movieId: Int): ApiResult<MovieDetail>
}
