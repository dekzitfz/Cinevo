package com.app.cinevo.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.cinevo.R

val InterFontFamily: FontFamily =
    FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
    )

private fun interStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Double,
): TextStyle =
    TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

/**
 * Material 3 type scale set in Inter. Sizes and line heights follow the M3 baseline; display and headline
 * styles use heavier weights and tighter tracking for a bolder, poster-like feel.
 */
internal val CinevoTypography: Typography =
    Typography(
        displayLarge = interStyle(FontWeight.Bold, size = 57, lineHeight = 64, letterSpacing = -0.5),
        displayMedium = interStyle(FontWeight.Bold, size = 45, lineHeight = 52, letterSpacing = -0.25),
        displaySmall = interStyle(FontWeight.SemiBold, size = 36, lineHeight = 44, letterSpacing = 0.0),
        headlineLarge = interStyle(FontWeight.SemiBold, size = 32, lineHeight = 40, letterSpacing = -0.25),
        headlineMedium = interStyle(FontWeight.SemiBold, size = 28, lineHeight = 36, letterSpacing = 0.0),
        headlineSmall = interStyle(FontWeight.SemiBold, size = 24, lineHeight = 32, letterSpacing = 0.0),
        titleLarge = interStyle(FontWeight.SemiBold, size = 22, lineHeight = 28, letterSpacing = 0.0),
        titleMedium = interStyle(FontWeight.SemiBold, size = 16, lineHeight = 24, letterSpacing = 0.15),
        titleSmall = interStyle(FontWeight.Medium, size = 14, lineHeight = 20, letterSpacing = 0.1),
        bodyLarge = interStyle(FontWeight.Normal, size = 16, lineHeight = 24, letterSpacing = 0.15),
        bodyMedium = interStyle(FontWeight.Normal, size = 14, lineHeight = 20, letterSpacing = 0.25),
        bodySmall = interStyle(FontWeight.Normal, size = 12, lineHeight = 16, letterSpacing = 0.4),
        labelLarge = interStyle(FontWeight.Medium, size = 14, lineHeight = 20, letterSpacing = 0.1),
        labelMedium = interStyle(FontWeight.Medium, size = 12, lineHeight = 16, letterSpacing = 0.5),
        labelSmall = interStyle(FontWeight.Medium, size = 11, lineHeight = 16, letterSpacing = 0.5),
    )
