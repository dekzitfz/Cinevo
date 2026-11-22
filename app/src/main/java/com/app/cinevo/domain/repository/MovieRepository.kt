package com.app.cinevo.domain.repository

import com.app.cinevo.core.network.ApiResult
import com.app.cinevo.domain.model.Genre

interface MovieRepository {
    suspend fun getGenres(): ApiResult<List<Genre>>
}
