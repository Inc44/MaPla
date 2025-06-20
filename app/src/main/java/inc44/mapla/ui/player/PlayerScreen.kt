package inc44.mapla.ui.player

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
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import inc44.mapla.drive.DriveCacheManager
import inc44.mapla.drive.DriveServiceHelper
import inc44.mapla.media.TrackInfoParser
import inc44.mapla.playlist.PlaylistLibrary
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(driveHelper: DriveServiceHelper, onAddPlaylist: () -> Unit) {
    /* data */
    val playlists by PlaylistLibrary.playlists.collectAsState()
    var listIndex by remember { mutableStateOf(0) }
    var trackIndex by remember { mutableStateOf(0) }

    /* player */
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) { onDispose { player.release() } }

    val cache = remember { DriveCacheManager(context, driveHelper) }

    LaunchedEffect(playlists) {
        if (listIndex >= playlists.size) listIndex = 0
        val n = playlists.getOrNull(listIndex)?.tracks?.size ?: 0
        if (trackIndex >= n) trackIndex = 0
    }

    if (playlists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No playlists") }
        return
    }

    val currentPlaylist = playlists[listIndex]
    val tracks = currentPlaylist.tracks
    val track = tracks.getOrNull(trackIndex)

    /* cache file */
    val localFile by
    produceState<File?>(initialValue = null, key1 = track) {
        value = track?.let { cache.get(it.id) }
    }

    /* playback */
    LaunchedEffect(localFile) {
        localFile?.let {
            player.setMediaItem(MediaItem.fromUri(it.toUri()))
            player.prepare()
            player.play()
        }
    }

    /* artwork */
    val artwork by
    produceState<ImageBitmap?>(initialValue = null, key1 = localFile) {
        value =
            withContext(Dispatchers.IO) {
                localFile?.let {
                    runCatching {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(it.absolutePath)
                        val bytes = mmr.embeddedPicture
                        mmr.release()
                        bytes?.let { b ->
                            BitmapFactory.decodeByteArray(b, 0, b.size).asImageBitmap()
                        }
                    }
                        .getOrNull()
                }
            }
    }

    /* text lines */
    val lines =
        remember(track) {
            track?.let {
                val fname = it.location.substringAfterLast('/')
                val parsed = TrackInfoParser.parse(fname)
                when {
                    parsed?.track != null ->
                        listOfNotNull(parsed.track, parsed.artist, parsed.album, parsed.year)
                    parsed?.title != null -> listOfNotNull(parsed.title, parsed.uploadDate)
                    else -> listOf(TrackInfoParser.clean(fname))
                }
            } ?: emptyList()
        }

    val isPlaying by remember { derivedStateOf { player.isPlaying } }

    Scaffold(topBar = { TopAppBar(title = { Text("Playing") }) }) { inner ->
        Column(
            Modifier.padding(inner).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /* now playing */
            Row(
                Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(96.dp).border(1.dp, MaterialTheme.colorScheme.outline),
                    Alignment.Center
                ) {
                    artwork?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    lines.forEach { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }

            /* playlists */
            LazyRow(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(playlists) { idx, pl ->
                    Text(
                        pl.title,
                        color =
                            if (idx == listIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.clickable {
                                listIndex = idx
                                trackIndex = 0
                            }
                    )
                }
                item {
                    IconButton(onClick = onAddPlaylist) {
                        Icon(Icons.Default.Add, contentDescription = "New playlist")
                    }
                }
            }

            /* songs */
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(tracks) { idx, t ->
                    val name = TrackInfoParser.clean(t.location.substringAfterLast('/'))
                    val highlight = idx == trackIndex
                    Text(
                        name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier.fillMaxWidth()
                                .background(
                                    if (highlight)
                                        MaterialTheme.colorScheme.primary.copy(alpha = .1f)
                                    else Color.Transparent
                                )
                                .clickable { trackIndex = idx }
                                .padding(4.dp)
                    )
                }
            }

            /* transport */
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        trackIndex = if (trackIndex > 0) trackIndex - 1 else tracks.lastIndex
                    }
                ) {
                    Icon(Icons.Default.SkipPrevious, null)
                }

                IconButton(onClick = { if (isPlaying) player.pause() else player.play() }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                }

                IconButton(
                    onClick = {
                        trackIndex = if (trackIndex < tracks.lastIndex) trackIndex + 1 else 0
                    }
                ) {
                    Icon(Icons.Default.SkipNext, null)
                }
            }
        }
    }
}
