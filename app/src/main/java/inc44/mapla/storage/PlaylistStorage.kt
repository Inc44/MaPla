package inc44.mapla.storage

import android.content.Context
import android.net.Uri
import inc44.mapla.playlist.Playlist
import inc44.mapla.playlist.XspfConverter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object PlaylistStorage {

    fun exportPlaylist(context: Context, playlist: Playlist, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use {
                    it.write(XspfConverter.toXml(playlist))
                }
                true
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun importPlaylist(context: Context, uri: Uri): Playlist? {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                XspfConverter.fromXml(InputStreamReader(it).readText())
            }
        } catch (_: Exception) {
            null
        }
    }
}
