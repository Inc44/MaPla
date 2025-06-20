package inc44.mapla.drive

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveCacheManager(context: Context, private val helper: DriveServiceHelper) {

    private val root = File(context.cacheDir, "drive").apply { mkdirs() }

    suspend fun get(fileId: String): File? =
        withContext(Dispatchers.IO) {
            if (fileId.isBlank()) return@withContext null // ← prevents “directory as file”
            val dst = File(root, fileId)
            if (dst.exists()) return@withContext dst
            runCatching { helper.downloadFile(fileId, dst) }.onFailure { dst.delete() }
            if (dst.exists() && dst.isFile) dst else null
        }
}
