package com.app.cinevo.core.network

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {
    @Test
    fun `returns Success when call succeeds`() =
        runTest {
            assertEquals(ApiResult.Success(42), safeApiCall { 42 })
        }

    @Test
    fun `maps IOException to Network error`() =
        runTest {
            val result = safeApiCall { throw IOException("offline") }

            assertEquals(ApiResult.Error(ApiError.Network), result)
        }

    @Test
    fun `maps HttpException to Http error with status code`() =
        runTest {
            val response = Response.error<Unit>(404, "".toResponseBody())

            val result = safeApiCall { throw HttpException(response) }

            assertEquals(ApiResult.Error(ApiError.Http(404)), result)
        }

    @Test
    fun `maps other exceptions to Unknown error`() =
        runTest {
            val result = safeApiCall { error("boom") }

            assertTrue((result as ApiResult.Error).error is ApiError.Unknown)
        }
}
