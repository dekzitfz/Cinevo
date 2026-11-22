package com.app.cinevo.presentation.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.app.cinevo.domain.model.Movie
import com.app.cinevo.domain.usecase.GetMoviesByGenreUseCase
import com.app.cinevo.presentation.navigation.MovieList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getMoviesByGenre: GetMoviesByGenreUseCase,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<MovieList>()

        val genreName: String = route.genreName

        val movies: Flow<PagingData<Movie>> = getMoviesByGenre(route.genreId).cachedIn(viewModelScope)
    }
