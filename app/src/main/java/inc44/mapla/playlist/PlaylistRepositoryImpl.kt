package inc44.mapla.playlist

import android.content.Context
import android.net.Uri
import inc44.mapla.storage.PlaylistStorage

class PlaylistRepositoryImpl : PlaylistRepository {
    override fun export(context: Context, playlist: Playlist, dst: Uri) =
        PlaylistStorage.exportPlaylist(context, playlist, dst)

    override fun import(context: Context, src: Uri) = PlaylistStorage.importPlaylist(context, src)
}
