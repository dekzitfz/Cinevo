package com.app.cinevo.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Cinevo's design system. Dark-first, follows the system dark/light setting, and deliberately does not use
 * dynamic (wallpaper) color so the brand palette stays consistent across devices.
 *
 * System bar icon contrast is handled by `enableEdgeToEdge()` in the activity, which also follows the system
 * setting, so it always agrees with [darkTheme]'s default.
 */
@Composable
fun CinevoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val spacing = CinevoSpacing()
    CompositionLocalProvider(LocalCinevoSpacing provides spacing) {
        MaterialTheme(
            colorScheme = if (darkTheme) CinevoDarkColorScheme else CinevoLightColorScheme,
            typography = CinevoTypography,
            shapes = CinevoShapes,
            content = content,
        )
    }
}

/** Accessors for Cinevo tokens that Material 3 does not model. */
object CinevoTheme {
    val spacing: CinevoSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalCinevoSpacing.current
}
