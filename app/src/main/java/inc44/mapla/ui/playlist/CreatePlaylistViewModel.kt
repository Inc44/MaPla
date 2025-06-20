package inc44.mapla.ui.playlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import inc44.mapla.drive.DriveFile
import inc44.mapla.drive.DriveRepository
import inc44.mapla.playlist.Playlist
import inc44.mapla.playlist.PlaylistRepository
import inc44.mapla.playlist.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val driveRepo: DriveRepository,
    private val playlistRepo: PlaylistRepository,
    private val context: Context
) : ViewModel() {

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    private var pendingPlaylist: Playlist? = null

    fun buildPlaylist(name: String, files: List<DriveFile>, onReady: () -> Unit) {
        viewModelScope.launch {
            _status.value = "Building playlist..."
            val allFiles =
                files.flatMap {
                    if (it.isFolder) driveRepo.listAudioFilesRecursively(it.id) else listOf(it)
                }
            val tracks = allFiles.map { Track(location = driveRepo.getRelativePath(it.id)) }
            pendingPlaylist = Playlist(name, tracks)
            onReady()
        }
    }

    fun export(uri: android.net.Uri, onComplete: (Boolean) -> Unit) {
        val pl = pendingPlaylist ?: return
        val ok = playlistRepo.export(context, pl, uri)
        _status.value = if (ok) "Playlist saved" else "Failed to save"
        onComplete(ok)
    }
}
