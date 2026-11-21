package com.app.cinevo.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>

    data class Error(
        val error: ApiError,
    ) : ApiResult<Nothing>
}

sealed interface ApiError {
    data object Network : ApiError

    data class Http(
        val code: Int,
    ) : ApiError

    data class Unknown(
        val cause: Throwable,
    ) : ApiError
}
