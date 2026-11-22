package com.app.cinevo.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.app.cinevo.R
import com.app.cinevo.presentation.theme.CinevoTheme

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
) {
    StatusMessage(message = message, modifier = modifier)
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StatusMessage(message = message, modifier = modifier) {
        Button(onClick = onRetry) { Text(text = stringResource(R.string.retry)) }
    }
}

@Composable
private fun StatusMessage(
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(CinevoTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action()
    }
}
