package inc44.mapla.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveServiceHelper(context: Context, account: GoogleSignInAccount) {

    private val driveService: Drive by lazy {
        val credential =
            GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE)
            )
                .apply { selectedAccount = account.account }
        Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("MaPla")
            .build()
    }

    private fun DriveFile.isSupportedAudio(): Boolean =
        !isFolder &&
                DriveConstants.SUPPORTED_AUDIO_EXTENSIONS.any {
                    name.lowercase(Locale.ROOT).endsWith(".$it")
                }

    suspend fun createPlaylist(playlistName: String): String? =
        withContext(Dispatchers.IO) {
            val metadata =
                com.google.api.services.drive.model.File().apply {
                    name = "$playlistName.mapla.json"
                    mimeType = "application/json"
                    description = "MaPla playlist file"
                }
            val content =
                ByteArrayContent.fromString(
                    "application/json",
                    """{"name":"$playlistName","tracks":[]}"""
                )
            try {
                driveService.files().create(metadata, content).setFields("id").execute().id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun listChildren(folderId: String?): List<DriveFile> =
        withContext(Dispatchers.IO) {
            val query =
                if (folderId == null) "trashed=false and 'root' in parents"
                else "trashed=false and '$folderId' in parents"
            val files =
                driveService
                    .files()
                    .list()
                    .setQ(query)
                    .setFields("files(id,name,mimeType)")
                    .execute()
                    .files
            files.map {
                DriveFile(
                    id = it.id,
                    name = it.name,
                    isFolder = it.mimeType == "application/vnd.google-apps.folder",
                    mimeType = it.mimeType
                )
            }
        }

    suspend fun listAudioFilesInFolder(folderId: String): List<DriveFile> =
        withContext(Dispatchers.IO) { listChildren(folderId).filter { it.isSupportedAudio() } }

    suspend fun createPlaylistFromFiles(playlistName: String, files: List<DriveFile>): String? =
        withContext(Dispatchers.IO) {
            val json =
                """{"name":"$playlistName","tracks":[${files.joinToString(",") { "\"${it.id}\"" }}]}"""
            val metadata =
                com.google.api.services.drive.model.File().apply {
                    name = "$playlistName.mapla.json"
                    mimeType = "application/json"
                    description = "MaPla playlist file"
                }
            val content = ByteArrayContent.fromString("application/json", json)
            try {
                driveService.files().create(metadata, content).setFields("id").execute().id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun listAudioFilesRecursively(folderId: String): List<DriveFile> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<DriveFile>()
            val queue = ArrayDeque<String>().apply { add(folderId) }
            while (queue.isNotEmpty()) {
                val id = queue.removeFirst()
                val children = listChildren(id)
                for (child in children) {
                    if (child.isFolder) queue.add(child.id)
                    else if (child.isSupportedAudio()) result.add(child)
                }
            }
            result
        }

    suspend fun getRelativePath(fileId: String): String =
        withContext(Dispatchers.IO) {
            val parts = mutableListOf<String>()
            var currentId: String? = fileId
            while (currentId != null && currentId != "root") {
                val file =
                    driveService.files().get(currentId).setFields("id,name,parents").execute()
                parts.add(file.name)
                currentId = file.parents?.firstOrNull()
            }
            parts.reversed().joinToString("/")
        }

    suspend fun uploadPlaylistXspf(name: String, xmlContent: String): String? =
        withContext(Dispatchers.IO) {
            val metadata =
                com.google.api.services.drive.model.File().apply {
                    this.name = "$name.xspf"
                    mimeType = "application/xspf+xml"
                    description = "MaPla XSPF playlist"
                }
            val content = ByteArrayContent.fromString("application/xspf+xml", xmlContent)
            try {
                driveService.files().create(metadata, content).setFields("id").execute().id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
