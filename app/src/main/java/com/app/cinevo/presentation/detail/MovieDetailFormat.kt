package com.app.cinevo.presentation.detail

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
