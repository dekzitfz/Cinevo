package com.app.cinevo.core.network

/** Carries a typed [ApiError] through APIs that report failures as exceptions, such as Paging's `LoadResult.Error`. */
class ApiException(
    val error: ApiError,
) : Exception(error.toString(), (error as? ApiError.Unknown)?.cause)
