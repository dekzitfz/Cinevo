package com.app.cinevo.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.app.cinevo.R
import kotlin.time.Duration.Companion.minutes

/** A runtime split into whole hours and the minutes left over. */
internal data class Runtime(
    val hours: Int,
    val minutes: Int,
)

internal fun runtimeOf(totalMinutes: Int): Runtime =
    totalMinutes.minutes.toComponents { hours, minutes, _, _ -> Runtime(hours.toInt(), minutes) }

/** Short form such as "2h 15m", dropping a zero part ("2h", "45m"). */
@Composable
@ReadOnlyComposable
internal fun runtimeText(runtime: Runtime): String =
    when {
        runtime.hours == 0 -> stringResource(R.string.runtime_minutes, runtime.minutes)
        runtime.minutes == 0 -> stringResource(R.string.runtime_hours, runtime.hours)
        else -> stringResource(R.string.runtime_hours_minutes, runtime.hours, runtime.minutes)
    }

/** Spoken form of [runtimeText], such as "2 hours 15 minutes". */
@Composable
@ReadOnlyComposable
internal fun runtimeDescription(runtime: Runtime): String {
    val hours = pluralStringResource(R.plurals.duration_hours, runtime.hours, runtime.hours)
    val minutes = pluralStringResource(R.plurals.duration_minutes, runtime.minutes, runtime.minutes)
    return when {
        runtime.hours == 0 -> minutes
        runtime.minutes == 0 -> hours
        else -> stringResource(R.string.duration_hours_minutes, hours, minutes)
    }
}
