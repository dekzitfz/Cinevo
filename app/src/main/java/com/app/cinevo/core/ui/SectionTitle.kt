package com.app.cinevo.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.app.cinevo.presentation.theme.CinevoTheme

/** Title of a section within a screen. Screen readers announce it as a heading, so users can jump between them. */
@Composable
fun SectionTitle(
    @StringRes text: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(text),
        modifier = modifier.padding(top = CinevoTheme.spacing.xs).semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
    )
}
