package com.app.cinevo.core.network

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

@Suppress("TooGenericExceptionCaught")
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        ApiResult.Error(ApiError.Http(e.code()))
    } catch (_: IOException) {
        ApiResult.Error(ApiError.Network)
    } catch (e: Exception) {
        ApiResult.Error(ApiError.Unknown(e))
    }
