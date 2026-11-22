package com.app.cinevo.core.image

/** Builds TMDB image URLs from file paths such as `/abc.jpg`. Returns null when there is no image. */
object TmdbImageUrl {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"
    private const val POSTER_SIZE = "w342"
    private const val BACKDROP_SIZE = "w780"
    private const val PROFILE_SIZE = "w185"

    fun poster(filePath: String?): String? = build(filePath, POSTER_SIZE)

    fun backdrop(filePath: String?): String? = build(filePath, BACKDROP_SIZE)

    fun profile(filePath: String?): String? = build(filePath, PROFILE_SIZE)

    private fun build(
        filePath: String?,
        size: String,
    ): String? = filePath?.takeIf { it.isNotBlank() }?.let { "$BASE_URL$size$it" }
}
