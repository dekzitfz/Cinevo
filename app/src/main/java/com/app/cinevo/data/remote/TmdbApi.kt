package com.app.cinevo.data.remote

import com.app.cinevo.data.remote.dto.GenreListResponseDto
import com.app.cinevo.data.remote.dto.MovieListResponseDto
import retrofit2.http.GET

interface TmdbApi {
    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListResponseDto

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(): MovieListResponseDto
}
