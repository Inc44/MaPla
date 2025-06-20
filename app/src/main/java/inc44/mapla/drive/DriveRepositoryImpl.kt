package inc44.mapla.drive

class DriveRepositoryImpl(private val helper: DriveServiceHelper) : DriveRepository {
    override suspend fun listChildren(folderId: String?) = helper.listChildren(folderId)

    override suspend fun listAudioFilesRecursively(folderId: String) =
        helper.listAudioFilesRecursively(folderId)

    override suspend fun getRelativePath(fileId: String) = helper.getRelativePath(fileId)
}
