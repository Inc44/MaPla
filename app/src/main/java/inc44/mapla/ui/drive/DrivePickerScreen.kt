package inc44.mapla.ui.drive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import inc44.mapla.drive.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivePickerScreen(driveHelper: DriveServiceHelper, onFilesPicked: (List<DriveFile>) -> Unit) {
    val viewModel: DrivePickerViewModel =
        viewModel(
            factory =
                object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        @Suppress("UNCHECKED_CAST")
                        return DrivePickerViewModel(DriveRepositoryImpl(driveHelper)) as T
                    }
                }
        )

    val folderStack by viewModel.folderStack.collectAsState()
    val items by viewModel.items.collectAsState()
    val selectedFiles by viewModel.selected.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val currentFolder = folderStack.lastOrNull()
    val appBarTitle = currentFolder?.name ?: "My Drive"

    Scaffold(
        topBar = { TopAppBar(title = { Text(appBarTitle) }) },
        floatingActionButton = {
            if (selectedFiles.isNotEmpty()) {
                FloatingActionButton(onClick = { onFilesPicked(selectedFiles.toList()) }) {
                    Text("Add (${selectedFiles.size})")
                }
            }
        }
    ) { padding ->
        when {
            isLoading ->
                Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            errorMessage != null ->
                Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            else ->
                DrivePickerList(
                    modifier = Modifier.padding(padding),
                    items = items,
                    selectedFiles = selectedFiles,
                    currentFolder = currentFolder,
                    onNavigateUp = viewModel::navigateUp,
                    onItemClick = {
                        if (it.isFolder) viewModel.navigateInto(it) else viewModel.toggleItem(it)
                    },
                    onToggleItem = viewModel::toggleItem
                )
        }
    }
}

@Composable
private fun DrivePickerList(
    modifier: Modifier,
    items: List<DriveFile>,
    selectedFiles: Set<DriveFile>,
    currentFolder: DriveFile?,
    onNavigateUp: () -> Unit,
    onItemClick: (DriveFile) -> Unit,
    onToggleItem: (DriveFile) -> Unit
) {
    val allowedExt = DriveConstants.SUPPORTED_AUDIO_EXTENSIONS
    val sortedItems =
        items
            .filter {
                it.isFolder || allowedExt.any { ext -> it.name.lowercase().endsWith(".$ext") }
            }
            .sortedWith(compareBy<DriveFile> { !it.isFolder }.thenBy { it.name.lowercase() })

    LazyColumn(modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        if (currentFolder != null) {
            item("up") {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable(onClick = onNavigateUp)
                            .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("…go up…", style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider()
            }
        }

        items(sortedItems, key = { it.id }) { file ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { onItemClick(file) }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        if (file.isFolder) Icons.Default.Folder else Icons.Default.MusicNote,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(file.name, modifier = Modifier.weight(1f))
                Checkbox(
                    checked = selectedFiles.contains(file),
                    onCheckedChange = { onToggleItem(file) }
                )
            }
            HorizontalDivider()
        }
    }
}
