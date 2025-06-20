package inc44.mapla.ui.player

import android.app.Application
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import inc44.mapla.drive.DriveServiceHelper
import inc44.mapla.media.TrackInfoParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    driveHelper: DriveServiceHelper,
    onAddPlaylist: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: PlayerViewModel =
        viewModel(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(c: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PlayerViewModel(
                            ctx.applicationContext as Application,
                            driveHelper
                        ) as T
                    }
                }
        )

    val playlists by vm.playlists.collectAsState()
    val plIndex by vm.playlistIndex.collectAsState()
    val trkIndex by vm.trackIndex.collectAsState()
    val playing by vm.isPlaying.collectAsState()

    if (playlists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No playlists") }
        return
    }

    val tracks = playlists[plIndex].tracks
    val track = tracks.getOrNull(trkIndex)

    /* local file */
    val local by produceState<File?>(null, track) { value = track?.let { vm.cachedFile(it.id) } }

    /* artwork */
    val art by produceState<ImageBitmap?>(null, local) {
        value =
            withContext(Dispatchers.IO) {
                local?.let {
                    runCatching {
                        MediaMetadataRetriever().use { mmr ->
                            mmr.setDataSource(it.absolutePath)
                            mmr.embeddedPicture?.let { bytes ->
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    .asImageBitmap()
                            }
                        }
                    }.getOrNull()
                }
            }
    }

    /* lines */
    val lines =
        remember(track) {
            track?.let {
                val fn = it.location.substringAfterLast('/')
                TrackInfoParser.parse(fn)?.let { p ->
                    when {
                        p.track != null -> listOfNotNull(p.track, p.artist, p.album, p.year)
                        else -> listOfNotNull(p.title, p.uploadDate)
                    }
                } ?: listOf(TrackInfoParser.clean(fn))
            } ?: emptyList()
        }

    Scaffold(topBar = { TopAppBar(title = { Text("Playing") }) }) { pad ->
        Column(
            modifier = Modifier.padding(pad).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /* now playing */
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(96.dp).border(1.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    art?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column { lines.forEach { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
            }

            /* playlist tabs */
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(playlists) { i, pl ->
                    Text(
                        pl.title,
                        color =
                            if (i == plIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { vm.selectPlaylist(i) }
                    )
                }
                item {
                    IconButton(onClick = onAddPlaylist) { Icon(Icons.Default.Add, null) }
                }
            }

            /* track list */
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(tracks) { i, t ->
                    val name = TrackInfoParser.clean(t.location.substringAfterLast('/'))
                    val hi = i == trkIndex
                    Text(
                        name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier.fillMaxWidth()
                                .background(
                                    if (hi) MaterialTheme.colorScheme.primary.copy(alpha = .1f)
                                    else Color.Transparent
                                )
                                .clickable { vm.selectTrack(i) }
                                .padding(4.dp)
                    )
                }
            }

            /* transport */
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.prevTrack() }) {
                    Icon(Icons.Default.SkipPrevious, null)
                }
                IconButton(onClick = { vm.togglePlayPause() }) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null
                    )
                }
                IconButton(onClick = { vm.nextTrack() }) {
                    Icon(Icons.Default.SkipNext, null)
                }
            }
        }
    }
}