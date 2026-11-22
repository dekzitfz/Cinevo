package com.app.cinevo.presentation.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class MovieDetailFormatTest {
    @Test
    fun `formats hours and minutes`() {
        assertEquals("2h 15m", formatRuntime(135))
        assertEquals("1h 1m", formatRuntime(61))
    }

    @Test
    fun `drops zero hours`() {
        assertEquals("45m", formatRuntime(45))
    }

    @Test
    fun `drops zero minutes`() {
        assertEquals("2h", formatRuntime(120))
    }
}
