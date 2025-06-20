package inc44.mapla.playlist

data class Track(
    val id: String = "",
    val location: String,
    val duration: Long? = null,
    val bitrate: Int? = null,
    val artist: String? = null,
    val album: String? = null,
    val title: String? = null,
    val year: String? = null,
    val uploadDate: String? = null
)

data class Playlist(val title: String, val tracks: List<Track>)
