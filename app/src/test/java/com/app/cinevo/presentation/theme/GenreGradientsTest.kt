package com.app.cinevo.presentation.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GenreGradientsTest {
    @Test
    fun `same genre id always maps to the same gradient`() {
        assertSame(GenreGradients.forGenre(28), GenreGradients.forGenre(28))
    }

    @Test
    fun `ids map onto the palette by modulo`() {
        val size = GenreGradients.palette.size
        repeat(size) { index -> assertSame(GenreGradients.palette[index], GenreGradients.forGenre(index)) }
        assertSame(GenreGradients.palette[0], GenreGradients.forGenre(size))
    }

    @Test
    fun `negative ids still resolve to a palette entry`() {
        assertSame(GenreGradients.palette.last(), GenreGradients.forGenre(-1))
    }

    @Test
    fun `palette has eight distinct gradients`() {
        assertEquals(8, GenreGradients.palette.distinct().size)
    }
}
