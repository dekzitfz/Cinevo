package com.app.cinevo.data.remote

import com.app.cinevo.data.remote.dto.GenreListResponseDto
import com.app.cinevo.data.remote.dto.MovieDetailDto
import com.app.cinevo.data.remote.dto.MovieListResponseDto
import com.app.cinevo.data.remote.dto.PagedMovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListResponseDto

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(): MovieListResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedMovieResponseDto

    /** Details plus videos and reviews in one request. */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "videos,reviews",
    ): MovieDetailDto
}
