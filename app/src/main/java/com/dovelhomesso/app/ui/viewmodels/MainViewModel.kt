package com.dovelhomesso.app.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dovelhomesso.app.data.db.AppDatabase
import com.dovelhomesso.app.data.entities.*
import com.dovelhomesso.app.data.SearchResult
import com.dovelhomesso.app.data.exportimport.BackupManager
import com.dovelhomesso.app.data.repositories.AppRepository
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.util.ImageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

import com.dovelhomesso.app.DoveLhoMessoApp

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    val repository = (application as DoveLhoMessoApp).repository
    
    val backupManager = BackupManager(application, repository)
    
    // ========== State Flows ==========
    
    val rooms: StateFlow<List<HouseRoomEntity>> = repository.getAllRooms()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val containers: StateFlow<List<ContainerEntity>> = repository.getAllContainers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val spots: StateFlow<List<SpotEntity>> = repository.getAllSpots()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val favoriteContainers: StateFlow<List<ContainerEntity>> = repository.getFavoriteContainers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val favoriteSpots: StateFlow<List<SpotEntity>> = repository.getFavoriteSpots()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // ========== Search ==========
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private var searchJob: kotlinx.coroutines.Job? = null
    
    fun search(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _isSearching.value = true
            try {
                val results = repository.globalSearch(query)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    // ========== Scanner ==========
    
    private val _foundSpotEvent = MutableSharedFlow<Long>()
    val foundSpotEvent = _foundSpotEvent.asSharedFlow()
    
    fun findSpotByCode(code: String) {
        viewModelScope.launch {
            val spot = repository.getSpotByCode(code)
            if (spot != null) {
                _foundSpotEvent.emit(spot.id)
            }
        }
    }

    // ========== Recent Entries ==========
    
    private val _recentEntries = MutableStateFlow<List<AppRepository.RecentEntry>>(emptyList())
    val recentEntries: StateFlow<List<AppRepository.RecentEntry>> = _recentEntries.asStateFlow()
    
    fun loadRecentEntries() {
        viewModelScope.launch {
            _recentEntries.value = repository.getRecentEntries(10)
        }
    }
    
    // ========== Room Operations ==========
    
    fun getContainersByRoom(roomId: Long): Flow<List<ContainerEntity>> = 
        repository.getContainersByRoom(roomId)
    
    fun getRoomById(roomId: Long): Flow<HouseRoomEntity?> = 
        repository.getRoomByIdFlow(roomId)
    
    fun createRoom(name: String) {
        viewModelScope.launch {
            repository.insertRoom(HouseRoomEntity(name = name))
        }
    }
    
    fun updateRoom(room: HouseRoomEntity) {
        viewModelScope.launch {
            repository.updateRoom(room)
        }
    }
    
    fun deleteRoom(room: HouseRoomEntity) {
        viewModelScope.launch {
            repository.deleteRoom(room)
        }
    }
    
    // ========== Container Operations ==========
    
    fun getSpotsByContainer(containerId: Long): Flow<List<SpotEntity>> = 
        repository.getSpotsByContainer(containerId)
    
    fun getContainerById(containerId: Long): Flow<ContainerEntity?> = 
        repository.getContainerByIdFlow(containerId)
    
    fun createContainer(roomId: Long, name: String, type: ContainerType) {
        viewModelScope.launch {
            repository.insertContainer(
                ContainerEntity(roomId = roomId, name = name, type = type.name)
            )
        }
    }
    
    fun updateContainer(container: ContainerEntity) {
        viewModelScope.launch {
            repository.updateContainer(container)
        }
    }
    
    fun deleteContainer(container: ContainerEntity) {
        viewModelScope.launch {
            repository.deleteContainer(container)
        }
    }
    
    fun toggleContainerFavorite(container: ContainerEntity) {
        viewModelScope.launch {
            repository.toggleContainerFavorite(container.id, !container.isFavorite)
        }
    }
    
    // ========== Spot Operations ==========
    
    fun getItemsBySpot(spotId: Long): Flow<List<ItemEntity>> = 
        repository.getItemsBySpot(spotId)
    
    fun getDocumentsBySpot(spotId: Long): Flow<List<DocumentEntity>> = 
        repository.getDocumentsBySpot(spotId)
    
    fun getSpotById(spotId: Long): Flow<SpotEntity?> = 
        repository.getSpotByIdFlow(spotId)
    
    fun createSpot(containerId: Long, label: String) {
        viewModelScope.launch {
            repository.createSpotWithCode(containerId, label)
        }
    }
    
    fun updateSpot(spot: SpotEntity) {
        viewModelScope.launch {
            repository.updateSpot(spot)
        }
    }
    
    fun deleteSpot(spot: SpotEntity) {
        viewModelScope.launch {
            repository.deleteSpot(spot)
        }
    }
    
    fun toggleSpotFavorite(spot: SpotEntity) {
        viewModelScope.launch {
            repository.toggleSpotFavorite(spot.id, !spot.isFavorite)
        }
    }
    
    // ========== Item Operations ==========
    
    fun getItemById(itemId: Long): Flow<ItemEntity?> = 
        repository.getItemByIdFlow(itemId)
    
    fun createItem(
        name: String,
        spotId: Long,
        category: String? = null,
        keywords: String? = null,
        tags: String? = null,
        note: String? = null,
        imagePath: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val securePath = ImageHelper.copyImageToInternalStorage(getApplication(), imagePath) ?: imagePath
            
            repository.insertItem(
                ItemEntity(
                    name = name,
                    spotId = spotId,
                    category = category,
                    keywords = keywords,
                    tags = tags,
                    note = note,
                    imagePath = securePath
                )
            )
            loadRecentEntries()
        }
    }
    
    fun updateItem(item: ItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val securePath = ImageHelper.copyImageToInternalStorage(getApplication(), item.imagePath) ?: item.imagePath
            
            repository.updateItem(item.copy(imagePath = securePath))
            loadRecentEntries()
        }
    }
    
    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
            loadRecentEntries()
        }
    }
    
    fun duplicateItem(item: ItemEntity, newSpotId: Long? = null) {
        viewModelScope.launch {
            repository.insertItem(
                item.copy(
                    id = 0,
                    spotId = newSpotId ?: item.spotId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            loadRecentEntries()
        }
    }

    fun moveItem(itemId: Long, newSpotId: Long) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            if (item != null) {
                repository.updateItem(item.copy(spotId = newSpotId))
                loadRecentEntries()
            }
        }
    }
    
    fun moveItems(itemIds: List<Long>, newSpotId: Long) {
        viewModelScope.launch {
            itemIds.forEach { id ->
                val item = repository.getItemById(id)
                if (item != null) {
                    repository.updateItem(item.copy(spotId = newSpotId))
                }
            }
            loadRecentEntries()
        }
    }
    
    // ========== Document Operations ==========
    
    fun getDocumentById(documentId: Long): Flow<DocumentEntity?> = 
        repository.getDocumentByIdFlow(documentId)
    
    fun createDocument(
        title: String,
        spotId: Long,
        docType: String? = null,
        person: String? = null,
        expiryDate: Long? = null,
        tags: String? = null,
        note: String? = null,
        filePaths: List<String>? = null
    ) {
        viewModelScope.launch {
            val pathsJson = if (!filePaths.isNullOrEmpty()) {
                com.google.gson.Gson().toJson(filePaths)
            } else {
                null
            }
            
            val document = DocumentEntity(
                title = title,
                spotId = spotId,
                docType = docType,
                person = person,
                expiryDate = expiryDate,
                tags = tags,
                note = note,
                filePaths = pathsJson
            )
            repository.insertDocument(document)
            loadRecentEntries()
        }
    }
    
    fun updateDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.updateDocument(document)
            loadRecentEntries()
        }
    }
    
    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(document)
            loadRecentEntries()
        }
    }
    
    fun duplicateDocument(document: DocumentEntity, newSpotId: Long? = null) {
        viewModelScope.launch {
            repository.insertDocument(
                document.copy(
                    id = 0,
                    spotId = newSpotId ?: document.spotId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            loadRecentEntries()
        }
    }

    fun moveDocument(documentId: Long, newSpotId: Long) {
        viewModelScope.launch {
            val document = repository.getDocumentById(documentId)
            if (document != null) {
                repository.updateDocument(document.copy(spotId = newSpotId))
                loadRecentEntries()
            }
        }
    }
    
    fun moveDocuments(documentIds: List<Long>, newSpotId: Long) {
        viewModelScope.launch {
            documentIds.forEach { id ->
                val document = repository.getDocumentById(id)
                if (document != null) {
                    repository.updateDocument(document.copy(spotId = newSpotId))
                }
            }
            loadRecentEntries()
        }
    }
    
    // ========== Breadcrumb Helper ==========
    
    suspend fun getBreadcrumb(spotId: Long): String = repository.getBreadcrumbForSpot(spotId)
    
    // ========== Backup / Restore ==========
    
    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()
    
    sealed class BackupStatus {
        data object Idle : BackupStatus()
        data object InProgress : BackupStatus()
        data class Success(val message: String) : BackupStatus()
        data class Error(val message: String) : BackupStatus()
    }
    
    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.InProgress
            backupManager.exportToUri(uri).fold(
                onSuccess = {
                    _backupStatus.value = BackupStatus.Success("Backup completato con successo")
                },
                onFailure = { e ->
                    _backupStatus.value = BackupStatus.Error(e.message ?: "Errore durante il backup")
                }
            )
        }
    }
    
    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.InProgress
            backupManager.importFromUri(uri).fold(
                onSuccess = { stats ->
                    _backupStatus.value = BackupStatus.Success(
                        "Importati: ${stats.roomsCount} stanze, ${stats.containersCount} mobili, " +
                        "${stats.spotsCount} posizioni, ${stats.itemsCount} oggetti, ${stats.documentsCount} documenti"
                    )
                    loadRecentEntries()
                },
                onFailure = { e ->
                    _backupStatus.value = BackupStatus.Error(e.message ?: "Errore durante il ripristino")
                }
            )
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.InProgress
            try {
                repository.clearAllData()
                _backupStatus.value = BackupStatus.Success("Tutti i dati sono stati cancellati")
                loadRecentEntries()
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error(e.message ?: "Errore durante la cancellazione")
            }
        }
    }
    
    fun resetBackupStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
    
    fun generateBackupFilename(): String = backupManager.generateBackupFilename()
    
    // ========== Position Selection Helper ==========
    
    suspend fun getSelectedPosition(spotId: Long): SelectedPosition? {
        val spot = repository.getSpotById(spotId) ?: return null
        val container = repository.getContainerById(spot.containerId) ?: return null
        val room = repository.getRoomById(container.roomId) ?: return null
        return SelectedPosition(room, container, spot)
    }
    
    // ========== Init ==========
    
    // ========== Security / Lock State ==========
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0
    
    companion object {
        private const val LOCK_TIMEOUT_MS = 60000L // 60 seconds
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
        lastBackgroundTimestamp = 0
    }

    fun lockApp() {
        _isAppUnlocked.value = false
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun checkLockTimeout() {
        if (lastBackgroundTimestamp > 0) {
            val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
            if (elapsed > LOCK_TIMEOUT_MS) {
                lockApp()
            }
            // Reset timestamp because we are now "foregrounded" (either locked or safely unlocked)
            lastBackgroundTimestamp = 0
        }
    }

    init {
        // Recent entries will be loaded on demand, not at startup
    }
}
