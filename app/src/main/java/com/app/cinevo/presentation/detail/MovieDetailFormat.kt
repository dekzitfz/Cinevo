package com.app.cinevo.presentation.detail

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.minutes

/** Formats a runtime such as 135 minutes as "2h 15m", dropping a zero part ("2h", "45m"). */
internal fun formatRuntime(minutes: Int): String =
    minutes.minutes.toComponents { hours, mins, _, _ ->
        when {
            hours == 0L -> "${mins}m"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }

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
