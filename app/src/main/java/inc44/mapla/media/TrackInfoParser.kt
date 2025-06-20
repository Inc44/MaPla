package inc44.mapla.media

data class TrackParsed(
    val artist: String? = null,
    val album: String? = null,
    val track: String? = null,
    val year: String? = null,
    val title: String? = null,
    val uploadDate: String? = null
)

object TrackInfoParser {
    private val patternFull =
        Regex("""^\s*(.+?)\s*-\s*(.+?)\s*-\s*(.+?)\s*-\s*(\d{4})\s*\[[^\]]+]\.[^.]+$""")
    private val patternSimple = Regex("""^\s*(.+?)\s*-\s*(\d{8})\s*\[[^\]]+]\.[^.]+$""")

    fun parse(name: String): TrackParsed? {
        patternFull.matchEntire(name)?.let {
            val (artist, album, track, year) = it.destructured
            return TrackParsed(artist.trim(), album.trim(), track.trim(), year.trim())
        }
        patternSimple.matchEntire(name)?.let {
            val (title, date) = it.destructured
            return TrackParsed(title = title.trim(), uploadDate = date.trim())
        }
        return null
    }

    /* plain filename without [id] part or extension */
    fun clean(name: String): String =
        name.replace("""\s*\[[^\]]+]""".toRegex(), "").substringBeforeLast('.').trim()
}
