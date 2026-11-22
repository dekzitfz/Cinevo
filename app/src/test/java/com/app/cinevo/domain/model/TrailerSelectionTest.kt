package com.app.cinevo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TrailerSelectionTest {
    private fun video(
        key: String,
        type: String = "Trailer",
        official: Boolean = false,
        site: String = "YouTube",
        publishedAt: String? = "2024-01-01T00:00:00Z",
    ) = Video(key, site, type, official, publishedAt?.let(Instant::parse))

    @Test
    fun `prefers an official trailer over newer unofficial trailers and teasers`() {
        val videos =
            listOf(
                video("teaser", type = "Teaser", official = true, publishedAt = "2024-06-01T00:00:00Z"),
                video("fan", publishedAt = "2024-05-01T00:00:00Z"),
                video("official", official = true, publishedAt = "2024-01-01T00:00:00Z"),
            )

        assertEquals("official", selectTrailer(videos)?.key)
    }

    @Test
    fun `picks the newest official trailer`() {
        val videos =
            listOf(
                video("old", official = true, publishedAt = "2023-01-01T00:00:00Z"),
                video("undated", official = true, publishedAt = null),
                video("new", official = true, publishedAt = "2024-01-01T00:00:00Z"),
            )

        assertEquals("new", selectTrailer(videos)?.key)
    }

    @Test
    fun `falls back to any trailer when none is official`() {
        val videos = listOf(video("teaser", type = "Teaser", official = true), video("trailer"))

        assertEquals("trailer", selectTrailer(videos)?.key)
    }

    @Test
    fun `falls back to the newest teaser when there is no trailer`() {
        val videos =
            listOf(
                video("clip", type = "Clip", official = true),
                video("teaser1", type = "Teaser", publishedAt = "2023-01-01T00:00:00Z"),
                video("teaser2", type = "Teaser", publishedAt = "2023-06-01T00:00:00Z"),
            )

        assertEquals("teaser2", selectTrailer(videos)?.key)
    }

    @Test
    fun `ignores videos that are not on YouTube`() {
        val videos =
            listOf(
                video("vimeo", official = true, site = "Vimeo", publishedAt = "2025-01-01T00:00:00Z"),
                video("youtube", type = "Teaser"),
            )

        assertEquals("youtube", selectTrailer(videos)?.key)
    }

    @Test
    fun `returns null when only non-YouTube trailers exist`() {
        assertNull(selectTrailer(listOf(video("vimeo", official = true, site = "Vimeo"))))
    }

    @Test
    fun `returns null when no video is a trailer or teaser`() {
        assertNull(selectTrailer(listOf(video("clip", type = "Clip"), video("bts", type = "Behind the Scenes"))))
    }

    @Test
    fun `returns null for an empty list`() {
        assertNull(selectTrailer(emptyList()))
    }
}
