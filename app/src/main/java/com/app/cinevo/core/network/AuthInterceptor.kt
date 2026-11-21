package com.app.cinevo.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val bearerToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain
                .request()
                .newBuilder()
                .header("Authorization", "Bearer $bearerToken")
                .header("Accept", "application/json")
                .build(),
        )
}
