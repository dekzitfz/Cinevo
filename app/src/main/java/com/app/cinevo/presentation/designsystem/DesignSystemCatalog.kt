package com.app.cinevo.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.app.cinevo.presentation.theme.CinevoTheme
import com.app.cinevo.presentation.theme.GenreGradients

// A few TMDB genre ids, to show how real ids map onto the palette.
private val SampleGenres =
    listOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        18 to "Drama",
        27 to "Horror",
        878 to "Sci-Fi",
    )

/**
 * Developer catalog of the Cinevo design system: color roles, type scale, shapes, spacing and genre gradients.
 * Edge-to-edge aware: content is padded by the safe-drawing insets so it clears the system bars and cutouts.
 */
@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    val spacing = CinevoTheme.spacing
    val contentPadding =
        WindowInsets.safeDrawing
            .add(WindowInsets(left = spacing.md, top = spacing.md, right = spacing.md, bottom = spacing.md))
            .asPaddingValues()

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            Text(
                text = "Cinevo design system",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item { Section(title = "Color") { ColorRoles() } }
        item { Section(title = "Typography") { TypeScale() } }
        item { Section(title = "Shapes") { ShapeSamples() } }
        item { Section(title = "Spacing") { SpacingScale() } }
        item { Section(title = "Genre gradients") { GenreGradientGrid() } }
    }
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

private data class ColorRole(
    val name: String,
    val container: Color,
    val content: Color,
)

@Composable
private fun ColorRoles(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val roles =
        listOf(
            ColorRole("primary", scheme.primary, scheme.onPrimary),
            ColorRole("primaryContainer", scheme.primaryContainer, scheme.onPrimaryContainer),
            ColorRole("secondary", scheme.secondary, scheme.onSecondary),
            ColorRole("secondaryContainer", scheme.secondaryContainer, scheme.onSecondaryContainer),
            ColorRole("tertiary", scheme.tertiary, scheme.onTertiary),
            ColorRole("tertiaryContainer", scheme.tertiaryContainer, scheme.onTertiaryContainer),
            ColorRole("background", scheme.background, scheme.onBackground),
            ColorRole("surface", scheme.surface, scheme.onSurface),
            ColorRole("surfaceVariant", scheme.surfaceVariant, scheme.onSurfaceVariant),
            ColorRole("surfaceContainerHigh", scheme.surfaceContainerHigh, scheme.onSurface),
            ColorRole("inverseSurface", scheme.inverseSurface, scheme.inverseOnSurface),
            ColorRole("error", scheme.error, scheme.onError),
        )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs)) {
        roles.forEach { role -> ColorSwatch(role) }
    }
}

@Composable
private fun ColorSwatch(
    role: ColorRole,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(role.container, MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                .padding(horizontal = CinevoTheme.spacing.sm),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = role.name, style = MaterialTheme.typography.labelLarge, color = role.content)
    }
}

@Composable
private fun TypeScale(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val styles: List<Pair<String, TextStyle>> =
        listOf(
            "Display Large" to typography.displayLarge,
            "Display Medium" to typography.displayMedium,
            "Display Small" to typography.displaySmall,
            "Headline Large" to typography.headlineLarge,
            "Headline Medium" to typography.headlineMedium,
            "Headline Small" to typography.headlineSmall,
            "Title Large" to typography.titleLarge,
            "Title Medium" to typography.titleMedium,
            "Title Small" to typography.titleSmall,
            "Body Large" to typography.bodyLarge,
            "Body Medium" to typography.bodyMedium,
            "Body Small" to typography.bodySmall,
            "Label Large" to typography.labelLarge,
            "Label Medium" to typography.labelMedium,
            "Label Small" to typography.labelSmall,
        )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.xs)) {
        styles.forEach { (name, style) ->
            Text(
                text = "$name · ${style.fontSize.value.toInt()}sp",
                style = style,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ShapeSamples(modifier: Modifier = Modifier) {
    val shapes = MaterialTheme.shapes
    val samples: List<Pair<String, Shape>> =
        listOf("small 8dp" to shapes.small, "medium 12dp" to shapes.medium, "large 16dp" to shapes.large)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(CinevoTheme.spacing.sm)) {
        samples.forEach { (name, shape) ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SpacingScale(modifier: Modifier = Modifier) {
    val spacing = CinevoTheme.spacing
    val steps =
        listOf(
            "xxs" to spacing.xxs,
            "xs" to spacing.xs,
            "sm" to spacing.sm,
            "md" to spacing.md,
            "lg" to spacing.lg,
            "xl" to spacing.xl,
        )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        steps.forEach { (name, size) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$name ${size.value.toInt()}dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(72.dp),
                )
                Box(
                    modifier =
                        Modifier
                            .size(width = size, height = spacing.sm)
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreGradientGrid(modifier: Modifier = Modifier) {
    val spacing = CinevoTheme.spacing
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        maxItemsInEachRow = 2,
    ) {
        GenreGradients.palette.forEachIndexed { index, gradient ->
            val genres = SampleGenres.filter { (id, _) -> GenreGradients.forGenre(id) == gradient }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(88.dp)
                        .background(gradient.brush, MaterialTheme.shapes.medium)
                        .padding(spacing.sm),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = "Gradient $index",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = genres.joinToString { (_, name) -> name }.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DesignSystemCatalogPreview() {
    CinevoTheme {
        DesignSystemCatalog()
    }
}
