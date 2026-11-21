package com.app.cinevo.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** A two-stop gradient. Both stops keep white text at or above 4.5:1 contrast. */
@Immutable
data class GenreGradient(
    val start: Color,
    val end: Color,
) {
    /** Diagonal top-start to bottom-end gradient that fills whatever it is drawn into. */
    val brush: Brush get() = Brush.linearGradient(listOf(start, end))
}

/**
 * Fixed palette of gradients for genre tiles. The same genre id always maps to the same gradient, in both
 * light and dark themes.
 */
object GenreGradients {
    val palette: List<GenreGradient> =
        listOf(
            GenreGradient(Color(0xFFE1292F), Color(0xFF8E1F5C)), // Ember
            GenreGradient(Color(0xFFBC5715), Color(0xFFA3283C)), // Sunset
            GenreGradient(Color(0xFF7554FF), Color(0xFF34188F)), // Violet
            GenreGradient(Color(0xFF1570EB), Color(0xFF1B3A8A)), // Ocean
            GenreGradient(Color(0xFF0E8279), Color(0xFF0B4F5C)), // Lagoon
            GenreGradient(Color(0xFF278452), Color(0xFF1A4636)), // Forest
            GenreGradient(Color(0xFFD6336C), Color(0xFF5F2C82)), // Orchid
            GenreGradient(Color(0xFF4B6CB7), Color(0xFF182848)), // Steel
        )

    fun forGenre(genreId: Int): GenreGradient = palette[Math.floorMod(genreId, palette.size)]
}
