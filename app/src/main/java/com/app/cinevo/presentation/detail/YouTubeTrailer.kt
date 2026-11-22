package com.app.cinevo.presentation.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.app.cinevo.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Embedded YouTube player with the video cued but not playing. Shows the video thumbnail until the player is
 * ready. The player follows the screen's lifecycle and is released when it leaves the composition.
 */
@Composable
fun YouTubeTrailerPlayer(
    videoKey: String,
    modifier: Modifier = Modifier,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var isCued by remember(videoKey) { mutableStateOf(false) }
    Box(modifier = modifier) {
        key(videoKey) {
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        enableAutomaticInitialization = false
                        lifecycle.addObserver(this)
                        initialize(
                            youTubePlayerListener =
                                object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        youTubePlayer.cueVideo(videoKey, startSeconds = 0f)
                                    }

                                    override fun onStateChange(
                                        youTubePlayer: YouTubePlayer,
                                        state: PlayerConstants.PlayerState,
                                    ) {
                                        if (state == PlayerConstants.PlayerState.VIDEO_CUED) isCued = true
                                    }
                                },
                            playerOptions =
                                IFramePlayerOptions
                                    .Builder(context)
                                    .controls(1)
                                    .autoplay(0)
                                    .build(),
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { view ->
                    lifecycle.removeObserver(view)
                    view.release()
                },
            )
        }
        if (!isCued) {
            AsyncImage(
                model = "https://img.youtube.com/vi/$videoKey/hqdefault.jpg",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** Opens the video in the YouTube app, or in a browser when the app is not installed. */
internal fun Context.openYouTubeVideo(videoKey: String) {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoKey".toUri())
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.trailer_no_app, Toast.LENGTH_SHORT).show()
    }
}
