package com.app.cinevo.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand
private val CinevoCoral = Color(0xFFE5484D)
private val CinevoCoralDeep = Color(0xFFC7343A)
private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)

// Dark palette: neutral, slightly cool blacks so posters and the coral accent carry the colour.
private val DarkBackground = Color(0xFF0F1014)
private val DarkSurface = Color(0xFF1A1B21)
private val DarkSurfaceVariant = Color(0xFF24252D)
private val DarkOnSurface = Color(0xFFECECF1)
private val DarkOnSurfaceVariant = Color(0xFFA9AAB6)

internal val CinevoDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = CinevoCoral,
        onPrimary = White,
        primaryContainer = Color(0xFF5C1A1D),
        onPrimaryContainer = Color(0xFFFFDAD8),
        inversePrimary = CinevoCoralDeep,
        secondary = Color(0xFFF5B544),
        onSecondary = Color(0xFF2A1C00),
        secondaryContainer = Color(0xFF3D2E10),
        onSecondaryContainer = Color(0xFFFFE0A8),
        tertiary = Color(0xFF5EC8D8),
        onTertiary = Color(0xFF00363D),
        tertiaryContainer = Color(0xFF0E3E45),
        onTertiaryContainer = Color(0xFFA6EEF9),
        background = DarkBackground,
        onBackground = DarkOnSurface,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceTint = CinevoCoral,
        inverseSurface = DarkOnSurface,
        inverseOnSurface = DarkSurface,
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF6B6C78),
        outlineVariant = Color(0xFF33343D),
        scrim = Black,
        surfaceBright = Color(0xFF34353E),
        surfaceDim = DarkBackground,
        surfaceContainerLowest = Color(0xFF0A0B0E),
        surfaceContainerLow = Color(0xFF15161B),
        surfaceContainer = DarkSurface,
        surfaceContainerHigh = DarkSurfaceVariant,
        surfaceContainerHighest = Color(0xFF2E2F38),
    )

// Light palette: the same hues, with a deeper coral so text-on-primary and primary-on-white stay above 4.5:1.
private val LightBackground = Color(0xFFF7F7FA)
private val LightSurfaceVariant = Color(0xFFECECF1)
private val LightOnSurface = Color(0xFF15161B)

internal val CinevoLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = CinevoCoralDeep,
        onPrimary = White,
        primaryContainer = Color(0xFFFFDAD8),
        onPrimaryContainer = Color(0xFF410006),
        inversePrimary = Color(0xFFFFB3AF),
        secondary = Color(0xFF825500),
        onSecondary = White,
        secondaryContainer = Color(0xFFFFDEA6),
        onSecondaryContainer = Color(0xFF2B1700),
        tertiary = Color(0xFF006874),
        onTertiary = White,
        tertiaryContainer = Color(0xFF9EEFFD),
        onTertiaryContainer = Color(0xFF001F24),
        background = LightBackground,
        onBackground = LightOnSurface,
        surface = White,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = Color(0xFF4A4B55),
        surfaceTint = CinevoCoralDeep,
        inverseSurface = Color(0xFF2A2B31),
        inverseOnSurface = Color(0xFFF1F1F5),
        error = Color(0xFFBA1A1A),
        onError = White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF7A7B86),
        outlineVariant = Color(0xFFC9CAD3),
        scrim = Black,
        surfaceBright = White,
        surfaceDim = Color(0xFFDCDCE3),
        surfaceContainerLowest = White,
        surfaceContainerLow = LightBackground,
        surfaceContainer = Color(0xFFF1F1F5),
        surfaceContainerHigh = LightSurfaceVariant,
        surfaceContainerHighest = Color(0xFFE6E6EC),
    )
