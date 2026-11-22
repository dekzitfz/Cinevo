package com.app.cinevo.core.ui

import com.app.cinevo.core.network.ApiError
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
