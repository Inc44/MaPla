package inc44.mapla.drive

interface DriveRepository {
    suspend fun listChildren(folderId: String? = null): List<DriveFile>

    suspend fun listAudioFilesRecursively(folderId: String): List<DriveFile>

    suspend fun getRelativePath(fileId: String): String
}
