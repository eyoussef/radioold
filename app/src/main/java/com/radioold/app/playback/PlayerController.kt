package com.radioold.app.playback

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.radioold.app.data.model.Track
import com.radioold.app.domain.Crossfade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Client for the [RadioService] media session. The player itself lives in a
 * foreground service, so the station keeps playing in the background and with
 * the screen off; this controller just drives it and exposes playback state as
 * flows. Commands issued before the connection completes are queued.
 */
class PlayerController(context: Context) {

    private var player: Player? = null
    private val pending = ArrayList<(Player) -> Unit>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _queue = MutableStateFlow<List<Track>>(emptyList())

    /** The playable queue as it stands (used by the "up next" UI). */
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()
    private var fadeInStart = 0L

    private val _current = MutableStateFlow<Track?>(null)
    val current: StateFlow<Track?> = _current.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying && fadeInStart == 0L) fadeInStart = System.currentTimeMillis()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            publishCurrent()
            fadeInStart = System.currentTimeMillis()
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) publishCurrent()
        }

        override fun onPlayerError(error: PlaybackException) {
            // One dead stream must never kill the radio: skip to the next track.
            Log.w(TAG, "stream failed (${error.errorCodeName}): ${error.message}")
            player?.let { if (it.hasNextMediaItem()) it.seekToNext() }
        }
    }

    init {
        val token = SessionToken(context, ComponentName(context, RadioService::class.java))
        MediaController.Builder(context, token).buildAsync().also { future ->
            future.addListener({
                runCatching {
                    val controller = future.get()
                    controller.addListener(listener)
                    player = controller
                    publishCurrent()
                    flushPending()
                }.onFailure { Log.w(TAG, "service connect failed: $it") }
            }, ContextCompat.getMainExecutor(context))
        }
        startMonitor()
    }

    private fun withPlayer(block: (Player) -> Unit) {
        val p = player
        if (p != null) block(p) else synchronized(pending) { pending.add(block) }
    }

    private fun flushPending() {
        val p = player ?: return
        val jobs = synchronized(pending) {
            val copy = ArrayList(pending)
            pending.clear()
            copy
        }
        jobs.forEach { it(p) }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        val playable = tracks.filter { it.stream != null }
        _queue.value = playable
        if (playable.isEmpty()) {
            // An empty station must go silent — never keep the previous
            // (wrong-year) music playing under a new selection.
            withPlayer {
                it.stop()
                it.clearMediaItems()
                publishCurrent()
            }
            return
        }
        val items = playable.map { MediaItem.Builder().setUri(it.stream!!).build() }
        withPlayer {
            it.setMediaItems(items, startIndex.coerceIn(0, items.size - 1), 0L)
            it.prepare()
            it.playWhenReady = true
        }
        publishCurrent()
        fadeInStart = System.currentTimeMillis()
    }

    fun toggle() = withPlayer { p -> if (p.isPlaying) p.pause() else p.play() }
    fun play() = withPlayer { it.play() }
    fun pause() = withPlayer { it.pause() }
    fun next() = withPlayer { if (it.hasNextMediaItem()) it.seekToNext() }
    fun prev() = withPlayer { if (it.hasPreviousMediaItem()) it.seekToPrevious() }
    fun seekTo(positionMs: Long) = withPlayer { it.seekTo(positionMs) }

    /** Jump straight to a queue position (the "up next" list). */
    fun playAt(index: Int) {
        if (index in _queue.value.indices) {
            withPlayer {
                it.seekTo(index, 0L)
                it.play()
            }
        }
    }

    /** Append freshly fetched station tracks (just-in-time queue top-up). */
    fun appendQueue(tracks: List<Track>) {
        val playable = tracks.filter { it.stream != null }
        if (playable.isEmpty()) return
        _queue.value = _queue.value + playable
        withPlayer { p -> p.addMediaItems(playable.map { MediaItem.Builder().setUri(it.stream!!).build() }) }
    }

    private fun publishCurrent() {
        _current.value = _queue.value.getOrNull(player?.currentMediaItemIndex ?: -1)
    }

    private fun startMonitor() {
        scope.launch {
            while (isActive) {
                val p = player
                _positionMs.value = p?.currentPosition ?: 0L
                _durationMs.value = if ((p?.duration ?: 0L) > 0L) p!!.duration else 0L
                applyFade()
                delay(100)
            }
        }
    }

    private fun applyFade() {
        val p = player ?: return
        val duration = p.duration
        val remaining = duration - p.currentPosition
        var volume = 1f
        if (fadeInStart != 0L) {
            val elapsed = System.currentTimeMillis() - fadeInStart
            volume = Crossfade.fadeIn(elapsed)
            if (elapsed >= Crossfade.DEFAULT_MS) fadeInStart = 0L
        }
        if (duration > 0 && remaining in 0..Crossfade.DEFAULT_MS) {
            volume = minOf(volume, Crossfade.fadeOut(remaining))
        }
        p.volume = volume.coerceIn(0f, 1f)
    }

    fun release() {
        scope.cancel()
        // Release the client only — the service keeps the station playing.
        player?.release()
        player = null
    }

    private companion object {
        const val TAG = "RadiooldPlayer"
    }
}
