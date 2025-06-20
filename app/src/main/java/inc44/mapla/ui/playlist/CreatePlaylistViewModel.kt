package inc44.mapla.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import inc44.mapla.drive.DriveFile
import inc44.mapla.drive.DriveRepository
import inc44.mapla.playlist.Playlist
import inc44.mapla.playlist.PlaylistLibrary
import inc44.mapla.playlist.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(private val driveRepo: DriveRepository) : ViewModel() {

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    fun buildPlaylist(name: String, files: List<DriveFile>, done: () -> Unit) {
        viewModelScope.launch {
            _status.value = "Building playlist…"
            val all =
                files.flatMap {
                    if (it.isFolder) driveRepo.listAudioFilesRecursively(it.id) else listOf(it)
                }
            val tracks = all.map { Track(id = it.id, location = driveRepo.getRelativePath(it.id)) }
            PlaylistLibrary.add(Playlist(name, tracks))
            _status.value = null
            done()
        }
    }
}
