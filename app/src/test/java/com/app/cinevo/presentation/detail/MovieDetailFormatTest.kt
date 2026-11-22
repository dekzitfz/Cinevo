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

    @Test
    fun `initials use the first letters of up to two words`() {
        assertEquals("JD", initialsOf("jane doe"))
        assertEquals("JM", initialsOf("  Jane   Mary Doe "))
        assertEquals("M", initialsOf("moviebuff"))
    }

    @Test
    fun `initials of a blank name are a question mark`() {
        assertEquals("?", initialsOf(""))
        assertEquals("?", initialsOf("   "))
    }
}
