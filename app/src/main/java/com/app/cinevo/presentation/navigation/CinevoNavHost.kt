package com.app.cinevo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.cinevo.presentation.detail.MovieDetailScreen
import com.app.cinevo.presentation.home.HomeScreen
import com.app.cinevo.presentation.movies.MovieListScreen
import com.app.cinevo.presentation.reviews.ReviewsScreen

@Composable
fun CinevoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Home, modifier = modifier) {
        composable<Home> {
            HomeScreen(
                onMovieClick = { movieId -> navController.navigate(MovieDetail(movieId)) },
                onGenreClick = { genreId, genreName -> navController.navigate(MovieList(genreId, genreName)) },
            )
        }
        composable<MovieList> {
            MovieListScreen(
                onMovieClick = { movieId -> navController.navigate(MovieDetail(movieId)) },
                onBackClick = navController::navigateUp,
            )
        }
        composable<MovieDetail> {
            MovieDetailScreen(
                onBackClick = navController::navigateUp,
                onSeeAllReviewsClick = { movieId, movieTitle -> navController.navigate(Reviews(movieId, movieTitle)) },
            )
        }
        composable<Reviews> {
            ReviewsScreen(onBackClick = navController::navigateUp)
        }
    }
}
