package com.app.cinevo.data.repository

import com.app.cinevo.core.network.ApiError
import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.data.remote.TmdbApi
import com.app.cinevo.di.NetworkModule
import com.app.cinevo.domain.model.Genre
import com.app.cinevo.domain.model.Movie
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `genres 200 maps genres to domain and calls the genre list endpoint`() =
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
    fun `genres 200 with no genres returns an empty list`() =
        runTest {
            server.enqueue(MockResponse(body = """{"genres":[]}"""))

            assertEquals(ApiResult.Success(emptyList<Genre>()), repository.getGenres())
        }

    @Test
    fun `genres 401 returns Http error`() = assertHttpError(401) { repository.getGenres() }

    @Test
    fun `genres 404 returns Http error`() = assertHttpError(404) { repository.getGenres() }

    @Test
    fun `genres 500 returns Http error`() = assertHttpError(500) { repository.getGenres() }

    @Test
    fun `genres malformed json returns Unknown error`() =
        assertMalformedJson("""{"genres":[{"id":"oops"}""") { repository.getGenres() }

    @Test
    fun `genres timeout returns Network error`() =
        runTest {
            server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())

            assertEquals(ApiResult.Error(ApiError.Network), repository.getGenres())
        }

    @Test
    fun `genres unreachable server returns Network error`() = assertNoNetwork { repository.getGenres() }

    @Test
    fun `trending 200 maps movies to domain and calls the first page of weekly trending`() =
        runTest {
            server.enqueue(
                MockResponse(
                    body =
                        """
                        {"page":1,"total_pages":500,"results":[
                          {"id":550,"title":"Fight Club","poster_path":"/poster.jpg","backdrop_path":"/backdrop.jpg",
                           "vote_average":8.4,"release_date":"1999-10-15","media_type":"movie"},
                          {"id":551,"title":"No Art","poster_path":null,"vote_average":6.0,"release_date":""}
                        ]}
                        """.trimIndent(),
                ),
            )

            val result = repository.getTrendingMovies()

            assertEquals(
                ApiResult.Success(
                    listOf(
                        Movie(550, "Fight Club", "/poster.jpg", "/backdrop.jpg", 8.4, "1999-10-15"),
                        Movie(551, "No Art", null, null, 6.0, null),
                    ),
                ),
                result,
            )
            val request = server.takeRequest()
            assertEquals("/trending/movie/week", request.url.encodedPath)
            assertNull(request.url.queryParameter("page"))
        }

    @Test
    fun `trending 200 with no results returns an empty list`() =
        runTest {
            server.enqueue(MockResponse(body = """{"page":1,"results":[]}"""))

            assertEquals(ApiResult.Success(emptyList<Movie>()), repository.getTrendingMovies())
        }

    @Test
    fun `trending 401 returns Http error`() = assertHttpError(401) { repository.getTrendingMovies() }

    @Test
    fun `trending 500 returns Http error`() = assertHttpError(500) { repository.getTrendingMovies() }

    @Test
    fun `trending malformed json returns Unknown error`() =
        assertMalformedJson("""{"results":[{"id":550,"title":null}]}""") { repository.getTrendingMovies() }

    @Test
    fun `trending unreachable server returns Network error`() = assertNoNetwork { repository.getTrendingMovies() }

    private fun assertHttpError(
        code: Int,
        call: suspend () -> ApiResult<*>,
    ) = runTest {
        server.enqueue(MockResponse(code = code, body = """{"status_message":"error"}"""))

        assertEquals(ApiResult.Error(ApiError.Http(code)), call())
    }

    private fun assertMalformedJson(
        body: String,
        call: suspend () -> ApiResult<*>,
    ) = runTest {
        server.enqueue(MockResponse(body = body))

        val error = (call() as ApiResult.Error).error

        assertTrue(error is ApiError.Unknown && error.cause is SerializationException)
    }

    private fun assertNoNetwork(call: suspend () -> ApiResult<*>) =
        runTest {
            server.close()

            assertEquals(ApiResult.Error(ApiError.Network), call())
        }
}
