package inc44.mapla.drive

data class DriveFile(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val mimeType: String? = null
)