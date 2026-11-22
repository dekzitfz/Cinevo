package com.app.cinevo.core.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbImageUrlTest {
    @Test
    fun `builds a w342 poster url`() {
        assertEquals("https://image.tmdb.org/t/p/w342/poster.jpg", TmdbImageUrl.poster("/poster.jpg"))
    }

    @Test
    fun `builds a w780 backdrop url`() {
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", TmdbImageUrl.backdrop("/backdrop.jpg"))
    }

    @Test
    fun `builds a w185 profile url`() {
        assertEquals("https://image.tmdb.org/t/p/w185/avatar.jpg", TmdbImageUrl.profile("/avatar.jpg"))
    }

    @Test
    fun `returns null for a null path`() {
        assertNull(TmdbImageUrl.poster(null))
        assertNull(TmdbImageUrl.backdrop(null))
        assertNull(TmdbImageUrl.profile(null))
    }

    @Test
    fun `returns null for a blank path`() {
        assertNull(TmdbImageUrl.poster(""))
        assertNull(TmdbImageUrl.backdrop("  "))
    }
}
