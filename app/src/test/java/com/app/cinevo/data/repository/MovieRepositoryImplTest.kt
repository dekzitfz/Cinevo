package com.app.cinevo.data.repository

import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.data.remote.TmdbApi
import com.app.cinevo.di.NetworkModule
import com.app.cinevo.domain.model.Genre
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

class MovieRepositoryImplTest {
    private val server = MockWebServer()
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().readTimeout(200, TimeUnit.MILLISECONDS).build()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(NetworkModule.provideJson().asConverterFactory("application/json".toMediaType()))
                .build()
                .create<TmdbApi>()
        repository = MovieRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `200 maps genres to domain and calls the genre list endpoint`() =
        runTest {
            server.enqueue(
                MockResponse(
                    body = """{"genres":[{"id":28,"name":"Action"},{"id":12,"name":"Adventure","extra":true}]}""",
                ),
            )

            val result = repository.getGenres()

            assertEquals(ApiResult.Success(listOf(Genre(28, "Action"), Genre(12, "Adventure"))), result)
            assertEquals("/genre/movie/list", server.takeRequest().url.encodedPath)
        }

    @Test
    fun `200 with no genres returns an empty list`() =
        runTest {
            server.enqueue(MockResponse(body = """{"genres":[]}"""))

            assertEquals(ApiResult.Success(emptyList<Genre>()), repository.getGenres())
        }

    @Test
    fun `401 returns Http error`() = assertHttpError(401)

    @Test
    fun `404 returns Http error`() = assertHttpError(404)

    @Test
    fun `500 returns Http error`() = assertHttpError(500)

    @Test
    fun `malformed json returns Unknown error`() =
        runTest {
            server.enqueue(MockResponse(body = """{"genres":[{"id":"oops"}"""))

            val error = (repository.getGenres() as ApiResult.Error).error

            assertTrue(error is ApiError.Unknown && error.cause is SerializationException)
        }

    @Test
    fun `timeout returns Network error`() =
        runTest {
            server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())

            assertEquals(ApiResult.Error(ApiError.Network), repository.getGenres())
        }

    @Test
    fun `unreachable server returns Network error`() =
        runTest {
            server.close()

            assertEquals(ApiResult.Error(ApiError.Network), repository.getGenres())
        }

    private fun assertHttpError(code: Int) =
        runTest {
            server.enqueue(MockResponse(code = code, body = """{"status_message":"error"}"""))

            assertEquals(ApiResult.Error(ApiError.Http(code)), repository.getGenres())
        }
}
