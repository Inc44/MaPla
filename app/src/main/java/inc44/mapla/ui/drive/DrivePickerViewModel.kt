package inc44.mapla.ui.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import inc44.mapla.drive.DriveFile
import inc44.mapla.drive.DriveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DrivePickerViewModel(private val repo: DriveRepository) : ViewModel() {

    private val _folderStack = MutableStateFlow(emptyList<DriveFile>())
    val folderStack: StateFlow<List<DriveFile>> = _folderStack

    private val _items = MutableStateFlow(emptyList<DriveFile>())
    val items: StateFlow<List<DriveFile>> = _items

    private val _selected = MutableStateFlow(emptySet<DriveFile>())
    val selected: StateFlow<Set<DriveFile>> = _selected

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        load()
    }

    private fun currentFolderId() = _folderStack.value.lastOrNull()?.id

    fun navigateInto(folder: DriveFile) {
        _folderStack.value += folder
        _selected.value = emptySet()
        load()
    }

    fun navigateUp() {
        if (_folderStack.value.isNotEmpty()) {
            _folderStack.value = _folderStack.value.dropLast(1)
            _selected.value = emptySet()
            load()
        }
    }

    fun toggleItem(file: DriveFile) {
        _selected.value = _selected.value.toMutableSet().apply { if (!add(file)) remove(file) }
    }

    private fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _items.value =
                try {
                    repo.listChildren(currentFolderId())
                } catch (e: Exception) {
                    _error.value = e.message
                    emptyList()
                }
            _loading.value = false
        }
    }
}
