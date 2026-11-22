package com.app.cinevo.presentation.reviews

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.app.cinevo.R
import com.app.cinevo.core.ui.EmptyView

/** All reviews of a movie. Shows only the movie title until the reviews list is built. */
@Composable
fun ReviewsScreen(
    movieTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.reviews_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        EmptyView(
            message = stringResource(R.string.reviews_placeholder, movieTitle),
            modifier = Modifier.padding(innerPadding),
        )
    }
}
