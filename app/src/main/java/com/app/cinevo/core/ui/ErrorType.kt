package com.app.cinevo.core.ui

import androidx.annotation.StringRes
import com.app.cinevo.R
import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiException
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

/** User-facing category of a failed request. */
enum class ErrorType { NoConnection, Server, Unauthorized, Unknown }

fun ApiError.toErrorType(): ErrorType =
    when (this) {
        ApiError.Network -> {
            ErrorType.NoConnection
        }

        is ApiError.Http -> {
            when {
                code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN -> ErrorType.Unauthorized
                code >= HTTP_INTERNAL_ERROR -> ErrorType.Server
                else -> ErrorType.Unknown
            }
        }

        is ApiError.Unknown -> {
            ErrorType.Unknown
        }
    }

/** For failures reported as exceptions, such as Paging load errors. Anything but an [ApiException] is Unknown. */
fun Throwable.toErrorType(): ErrorType = (this as? ApiException)?.error?.toErrorType() ?: ErrorType.Unknown

@StringRes
fun ErrorType.messageRes(): Int =
    when (this) {
        ErrorType.NoConnection -> R.string.error_no_connection
        ErrorType.Server -> R.string.error_server
        ErrorType.Unauthorized -> R.string.error_unauthorized
        ErrorType.Unknown -> R.string.error_unknown
    }
