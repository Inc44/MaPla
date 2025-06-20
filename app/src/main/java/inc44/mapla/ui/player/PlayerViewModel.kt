package inc44.mapla.ui.player

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import inc44.mapla.drive.DriveCacheManager
import inc44.mapla.drive.DriveServiceHelper
import inc44.mapla.playlist.PlaylistLibrary
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application, private val driveHelper: DriveServiceHelper) :
    AndroidViewModel(application), Player.Listener {

    private val exo =
        ExoPlayer.Builder(application).build().apply { addListener(this@PlayerViewModel) }

    private val cache = DriveCacheManager(application, driveHelper)

    val playlists = PlaylistLibrary.playlists

    private val _pl = MutableStateFlow(0)
    val playlistIndex: StateFlow<Int> = _pl

    private val _trk = MutableStateFlow(0)
    val trackIndex: StateFlow<Int> = _trk

    private val _playing = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _playing

    init {
        viewModelScope.launch {
            combine(playlists, _pl, _trk) { _, _, _ -> }.collect { prepareCurrent() }
        }
    }

    /* ── public control ────────────────────────────────────────────────────── */
    fun selectPlaylist(i: Int) {
        _pl.value = i.coerceIn(0, playlists.value.lastIndex)
        _trk.value = 0
    }

    fun selectTrack(i: Int) {
        playlists.value.getOrNull(_pl.value)?.tracks?.lastIndex?.let {
            _trk.value = i.coerceIn(0, it)
        }
    }

    fun prevTrack() {
        playlists.value.getOrNull(_pl.value)?.tracks?.lastIndex?.let {
            _trk.value = if (_trk.value > 0) _trk.value - 1 else it
        }
    }

    fun nextTrack() {
        playlists.value.getOrNull(_pl.value)?.tracks?.lastIndex?.let {
            _trk.value = if (_trk.value < it) _trk.value + 1 else 0
        }
    }

    fun togglePlayPause() {
        if (exo.mediaItemCount == 0) {
            prepareCurrent()
        } else {
            if (exo.isPlaying) exo.pause() else exo.play()
        }
    }

    suspend fun cachedFile(id: String): File? = withContext(Dispatchers.IO) { cache.get(id) }

    /* ── internals ─────────────────────────────────────────────────────────── */
    private fun prepareCurrent() {
        val track = playlists.value.getOrNull(_pl.value)?.tracks?.getOrNull(_trk.value) ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val file = cache.get(track.id) ?: return@launch
            withContext(Dispatchers.Main) {
                runCatching {
                    exo.setMediaItem(MediaItem.fromUri(file.toUri()))
                    exo.prepare()
                    exo.play()
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _playing.value = isPlaying
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_ENDED) nextTrack()
    }

    override fun onCleared() {
        exo.release()
    }
}
