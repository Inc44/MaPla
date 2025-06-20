package inc44.mapla.playlist

import android.util.Xml
import java.io.StringWriter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

object XspfConverter {

    fun toXml(playlist: Playlist): String {
        val writer = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("utf-8", true)
        serializer.startTag(null, "playlist")
        serializer.attribute(null, "xmlns", "http://xspf.org/ns/0/")
        serializer.attribute(null, "version", "1")
        serializer.startTag(null, "title")
        serializer.text(playlist.title)
        serializer.endTag(null, "title")
        serializer.startTag(null, "trackList")
        for (track in playlist.tracks) {
            serializer.startTag(null, "track")
            serializer.startTag(null, "location")
            serializer.text(track.location)
            serializer.endTag(null, "location")
            track.duration?.let {
                serializer.startTag(null, "duration")
                serializer.text(it.toString())
                serializer.endTag(null, "duration")
            }
            track.bitrate?.let {
                serializer.startTag(null, "meta")
                serializer.attribute(null, "rel", "bitrate")
                serializer.text(it.toString())
                serializer.endTag(null, "meta")
            }
            serializer.endTag(null, "track")
        }
        serializer.endTag(null, "trackList")
        serializer.endTag(null, "playlist")
        serializer.endDocument()
        return writer.toString()
    }

    fun fromXml(xml: String): Playlist {
        val parser =
            XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }.newPullParser()
        parser.setInput(xml.reader())

        var event = parser.eventType
        var title = ""
        val tracks = mutableListOf<Track>()
        var location: String? = null
        var duration: Long? = null
        var bitrate: Int? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG ->
                    when (parser.name) {
                        "title" -> {
                            parser.next()
                            title = parser.text
                        }
                        "location" -> {
                            parser.next()
                            location = parser.text
                        }
                        "duration" -> {
                            parser.next()
                            duration = parser.text.toLongOrNull()
                        }
                        "meta" ->
                            if (parser.getAttributeValue(null, "rel") == "bitrate") {
                                parser.next()
                                bitrate = parser.text.toIntOrNull()
                            }
                    }
                XmlPullParser.END_TAG ->
                    if (parser.name == "track") {
                        if (location != null) {
                            tracks.add(Track(location, duration, bitrate))
                        }
                        location = null
                        duration = null
                        bitrate = null
                    }
            }
            event = parser.next()
        }
        return Playlist(title, tracks)
    }
}
