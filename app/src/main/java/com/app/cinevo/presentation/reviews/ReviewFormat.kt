package com.app.cinevo.presentation.reviews

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Up to two initials from a name, such as "JD" for "john doe", or "?" when it has none. */
internal fun initialsOf(name: String): String =
    name
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Medium-length date in the device's locale and time zone, such as "Mar 4, 2024". */
internal fun formatDate(instant: Instant): String =
    DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(instant)

/**
 * Review text as plain prose. TMDB reviews are user-written and often carry Markdown or HTML markup, stray
 * whitespace and long runs of blank lines. Markers are dropped and their text kept; paragraphs stay one blank
 * line apart.
 */
internal fun cleanReviewContent(content: String): String =
    content
        .replace(LineEnding, "\n")
        .replace(HtmlLineBreak, "\n")
        .replace(HtmlParagraphBreak, "\n\n")
        .replace(HtmlTag, "")
        .replace(HtmlEntity) { HtmlEntities.getValue(it.groupValues[1]) }
        .replace(MarkdownLink, "$1")
        .replace(MarkdownLinePrefix, "")
        .replace(MarkdownRule, "")
        .replace(MarkdownEmphasis, "$2")
        .replace(TrailingSpaces, "")
        .replace(ExtraBlankLines, "\n\n")
        .trim()

private val LineEnding = Regex("""\r\n?""")
private val HtmlLineBreak = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
private val HtmlParagraphBreak = Regex("""</p>\s*<p[^>]*>""", RegexOption.IGNORE_CASE)
private val HtmlTag = Regex("""</?[a-zA-Z][^<>]*>""")
private val HtmlEntities = mapOf("amp" to "&", "quot" to "\"", "#39" to "'", "apos" to "'", "lt" to "<", "gt" to ">")
private val HtmlEntity = Regex("""&(${HtmlEntities.keys.joinToString("|")});""")
private val MarkdownLink = Regex("""\[([^\]]+)]\([^)\s]*\)""")

/** Heading (`# `) and quote (`> `) markers at the start of a line. */
private val MarkdownLinePrefix = Regex("""^[ \t]*(#{1,6}[ \t]+|>[ \t]?)""", RegexOption.MULTILINE)

/** A line of only `---`, `***` or `___`. */
private val MarkdownRule = Regex("""^[ \t]*([-*_])([ \t]*\1){2,}[ \t]*$""", RegexOption.MULTILINE)

/** `**bold**`, `__bold__`, `*italic*`, `_italic_` and `~~struck~~` within a line, but not `snake_case` or `2 * 3`. */
private val MarkdownEmphasis = Regex("""(?<![\w*~])(\*\*|__|~~|\*|_)(?=\S)(.+?)(?<=\S)\1(?![\w*~])""")
private val TrailingSpaces = Regex("""[ \t]+$""", RegexOption.MULTILINE)
private val ExtraBlankLines = Regex("""\n{3,}""")
