package inc44.mapla.playlist

import android.content.Context
import android.net.Uri

interface PlaylistRepository {
    fun export(context: Context, playlist: Playlist, dst: Uri): Boolean
    fun import(context: Context, src: Uri): Playlist?
} 