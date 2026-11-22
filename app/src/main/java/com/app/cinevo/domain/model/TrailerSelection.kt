package com.app.cinevo.domain.model

private const val YOUTUBE = "YouTube"
private const val TRAILER = "Trailer"
private const val TEASER = "Teaser"

/**
 * Picks the video to show as a movie's trailer: YouTube only, preferring an official trailer, then any trailer,
 * then a teaser, and the newest within each. Returns null when nothing qualifies.
 */
fun selectTrailer(videos: List<Video>): Video? {
    val candidates = videos.filter { it.site == YOUTUBE }.sortedByDescending { it.publishedAt }
    return candidates.firstOrNull { it.type == TRAILER && it.official }
        ?: candidates.firstOrNull { it.type == TRAILER }
        ?: candidates.firstOrNull { it.type == TEASER }
}
