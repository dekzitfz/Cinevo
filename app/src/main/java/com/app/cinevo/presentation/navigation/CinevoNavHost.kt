package com.app.cinevo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.cinevo.presentation.genres.GenreScreen
import com.app.cinevo.presentation.movies.MovieListScreen

@Composable
fun CinevoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Genres, modifier = modifier) {
        composable<Genres> {
            GenreScreen(onGenreClick = { genreId, genreName -> navController.navigate(MovieList(genreId, genreName)) })
        }
        composable<MovieList> { backStackEntry ->
            MovieListScreen(
                genreName = backStackEntry.toRoute<MovieList>().genreName,
                onBackClick = navController::navigateUp,
            )
        }
    }
}
