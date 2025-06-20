package inc44.mapla.ui.playlist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import inc44.mapla.drive.DriveFile
import inc44.mapla.drive.DriveRepositoryImpl
import inc44.mapla.drive.DriveServiceHelper
import inc44.mapla.playlist.PlaylistRepositoryImpl

@Composable
fun CreatePlaylistScreen(
    driveHelper: DriveServiceHelper,
    selectedFiles: List<DriveFile>,
    onDone: () -> Unit = {}
) {
    val defaultName =
        remember(selectedFiles) {
            selectedFiles.firstOrNull()?.name?.substringBefore('.') ?: "New Playlist"
        }

    var playlistName by remember { mutableStateOf(defaultName) }
    val context = LocalContext.current

    val viewModel: CreatePlaylistViewModel =
        viewModel(
            factory =
                object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        @Suppress("UNCHECKED_CAST")
                        return CreatePlaylistViewModel(
                            driveRepo = DriveRepositoryImpl(driveHelper),
                            playlistRepo = PlaylistRepositoryImpl(),
                            context = context
                        )
                                as T
                    }
                }
        )

    val status by viewModel.status.collectAsState()

    val createDocLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/xspf+xml")
        ) { uri ->
            uri?.let { viewModel.export(it) { if (it) onDone() } }
        }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create playlist with ${selectedFiles.size} tracks")
        TextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text("Playlist name") },
            singleLine = true
        )
        Button(
            enabled = playlistName.isNotBlank(),
            onClick = {
                viewModel.buildPlaylist(playlistName, selectedFiles) {
                    createDocLauncher.launch("$playlistName.xspf")
                }
            }
        ) {
            Text("Save playlist")
        }
        if (!status.isNullOrBlank()) Text(status!!)
    }
}
