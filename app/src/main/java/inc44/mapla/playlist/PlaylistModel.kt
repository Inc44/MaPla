package inc44.mapla.playlist

data class Track(
    val location: String,
    val duration: Long? = null,
    val bitrate: Int? = null
)

data class Playlist(
    val title: String,
    val tracks: List<Track>
) 