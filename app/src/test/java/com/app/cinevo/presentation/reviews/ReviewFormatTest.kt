package com.app.cinevo.presentation.reviews

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewFormatTest {
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

    @Test
    fun `plain text is unchanged`() {
        val text = "A fine film, 2 * 3 stars. Uses snake_case names & <3 for love."

        assertEquals(text, cleanReviewContent(text))
    }

    @Test
    fun `surrounding whitespace and trailing spaces are trimmed`() {
        assertEquals("First line\nSecond line", cleanReviewContent("  \n First line   \nSecond line\t\n\n "))
    }

    @Test
    fun `runs of blank lines collapse to one`() {
        assertEquals("One.\n\nTwo.", cleanReviewContent("One.\r\n\r\n\r\n   \n\nTwo."))
    }

    @Test
    fun `markdown emphasis keeps its text`() {
        assertEquals(
            "bold, bold, italic, italic and struck",
            cleanReviewContent("**bold**, __bold__, *italic*, _italic_ and ~~struck~~"),
        )
    }

    @Test
    fun `markdown links keep their text`() {
        assertEquals("See the trailer here.", cleanReviewContent("See the trailer [here](https://youtu.be/x)."))
    }

    @Test
    fun `markdown headings, quotes and rules are removed`() {
        assertEquals(
            "Verdict\n\nWorth it.\n\nquoted line",
            cleanReviewContent("## Verdict\n\nWorth it.\n\n---\n\n> quoted line"),
        )
    }

    @Test
    fun `html tags are removed and line breaks kept`() {
        assertEquals(
            "Great film.\nReally great.\n\nNew paragraph with emphasis.",
            cleanReviewContent(
                "<p>Great <b>film</b>.<br/>Really great.</p><p>New paragraph with <em>emphasis</em>.</p>",
            ),
        )
    }

    @Test
    fun `html entities are decoded once`() {
        assertEquals(
            "Tom & Jerry's \"<b>\" &lt;",
            cleanReviewContent("Tom &amp; Jerry&#39;s &quot;&lt;b&gt;&quot; &amp;lt;"),
        )
    }

    @Test
    fun `blank content stays blank`() {
        assertEquals("", cleanReviewContent(" \n\n <br> "))
    }
}
