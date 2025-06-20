package inc44.mapla.playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlaylistLibrary {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    fun add(playlist: Playlist) {
        _playlists.value = _playlists.value + playlist
    }
}
