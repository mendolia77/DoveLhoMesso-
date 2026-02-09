package com.dovelhomesso.app.data.repositories

import com.dovelhomesso.app.data.dao.*
import com.dovelhomesso.app.data.entities.*
import com.dovelhomesso.app.util.SpotCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AppRepository(
    private val houseRoomDao: HouseRoomDao,
    private val containerDao: ContainerDao,
    private val spotDao: SpotDao,
    private val itemDao: ItemDao,
    private val documentDao: DocumentDao
) {
    // ========== House Rooms ==========
    
    fun getAllRooms(): Flow<List<HouseRoomEntity>> = houseRoomDao.getAllRooms()
    
    fun getRoomByIdFlow(id: Long): Flow<HouseRoomEntity?> = houseRoomDao.getRoomByIdFlow(id)
    
    suspend fun getRoomById(id: Long): HouseRoomEntity? = houseRoomDao.getRoomById(id)
    
    suspend fun insertRoom(room: HouseRoomEntity): Long = houseRoomDao.insertRoom(room)
    
    suspend fun updateRoom(room: HouseRoomEntity) = houseRoomDao.updateRoom(
        room.copy(updatedAt = System.currentTimeMillis())
    )
    
    suspend fun deleteRoom(room: HouseRoomEntity) = houseRoomDao.deleteRoom(room)
    
    suspend fun deleteRoomById(id: Long) = houseRoomDao.deleteRoomById(id)
    
    // ========== Containers ==========
    
    fun getAllContainers(): Flow<List<ContainerEntity>> = containerDao.getAllContainers()
    
    fun getContainersByRoom(roomId: Long): Flow<List<ContainerEntity>> = 
        containerDao.getContainersByRoom(roomId)
    
    fun getContainerByIdFlow(id: Long): Flow<ContainerEntity?> = containerDao.getContainerByIdFlow(id)
    
    suspend fun getContainerById(id: Long): ContainerEntity? = containerDao.getContainerById(id)
    
    fun getFavoriteContainers(): Flow<List<ContainerEntity>> = containerDao.getFavoriteContainers()
    
    suspend fun insertContainer(container: ContainerEntity): Long = 
        containerDao.insertContainer(container)
    
    suspend fun updateContainer(container: ContainerEntity) = containerDao.updateContainer(
        container.copy(updatedAt = System.currentTimeMillis())
    )
    
    suspend fun deleteContainer(container: ContainerEntity) = containerDao.deleteContainer(container)
    
    suspend fun deleteContainerById(id: Long) = containerDao.deleteContainerById(id)
    
    suspend fun toggleContainerFavorite(id: Long, isFavorite: Boolean) = 
        containerDao.updateFavorite(id, isFavorite)
    
    // ========== Spots ==========
    
    fun getAllSpots(): Flow<List<SpotEntity>> = spotDao.getAllSpots()
    
    fun getSpotsByContainer(containerId: Long): Flow<List<SpotEntity>> = 
        spotDao.getSpotsByContainer(containerId)
    
    fun getSpotByIdFlow(id: Long): Flow<SpotEntity?> = spotDao.getSpotByIdFlow(id)
    
    suspend fun getSpotById(id: Long): SpotEntity? = spotDao.getSpotById(id)
    
    fun getFavoriteSpots(): Flow<List<SpotEntity>> = spotDao.getFavoriteSpots()
    
    suspend fun insertSpot(spot: SpotEntity): Long = spotDao.insertSpot(spot)
    
    suspend fun createSpotWithCode(
        containerId: Long,
        label: String,
        note: String? = null,
        isFavorite: Boolean = false
    ): Long {
        val container = containerDao.getContainerById(containerId)
            ?: throw IllegalArgumentException("Container not found")
        val room = houseRoomDao.getRoomById(container.roomId)
            ?: throw IllegalArgumentException("Room not found")
        
        val existingCodes = spotDao.getCodesWithPrefix("")
        val code = SpotCodeGenerator.generateCode(room.name, container.name, label, existingCodes)
        
        val spot = SpotEntity(
            containerId = containerId,
            label = label,
            code = code,
            note = note,
            isFavorite = isFavorite
        )
        return spotDao.insertSpot(spot)
    }
    
    suspend fun updateSpot(spot: SpotEntity) = spotDao.updateSpot(
        spot.copy(updatedAt = System.currentTimeMillis())
    )
    
    suspend fun deleteSpot(spot: SpotEntity) = spotDao.deleteSpot(spot)
    
    suspend fun deleteSpotById(id: Long) = spotDao.deleteSpotById(id)
    
    suspend fun toggleSpotFavorite(id: Long, isFavorite: Boolean) = 
        spotDao.updateFavorite(id, isFavorite)
    
    // ========== Items ==========
    
    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAllItems()
    
    fun getItemsBySpot(spotId: Long): Flow<List<ItemEntity>> = itemDao.getItemsBySpot(spotId)
    
    fun getItemByIdFlow(id: Long): Flow<ItemEntity?> = itemDao.getItemByIdFlow(id)
    
    suspend fun getItemById(id: Long): ItemEntity? = itemDao.getItemById(id)
    
    fun getRecentItems(limit: Int = 10): Flow<List<ItemEntity>> = itemDao.getRecentItems(limit)
    
    suspend fun searchItems(query: String): List<ItemEntity> = itemDao.searchItems(query)
    
    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)
    
    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(
        item.copy(updatedAt = System.currentTimeMillis())
    )
    
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)
    
    suspend fun deleteItemById(id: Long) = itemDao.deleteItemById(id)
    
    // ========== Documents ==========
    
    fun getAllDocuments(): Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    suspend fun getAllDocumentsList(): List<DocumentEntity> = documentDao.getAllDocumentsList()
    
    fun getDocumentsBySpot(spotId: Long): Flow<List<DocumentEntity>> = 
        documentDao.getDocumentsBySpot(spotId)
    
    fun getDocumentByIdFlow(id: Long): Flow<DocumentEntity?> = documentDao.getDocumentByIdFlow(id)
    
    suspend fun getDocumentById(id: Long): DocumentEntity? = documentDao.getDocumentById(id)
    
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>> = 
        documentDao.getRecentDocuments(limit)
    
    suspend fun searchDocuments(query: String): List<DocumentEntity> = 
        documentDao.searchDocuments(query)
    
    suspend fun insertDocument(document: DocumentEntity): Long = 
        documentDao.insertDocument(document)
    
    suspend fun updateDocument(document: DocumentEntity) = documentDao.updateDocument(
        document.copy(updatedAt = System.currentTimeMillis())
    )
    
    suspend fun deleteDocument(document: DocumentEntity) = documentDao.deleteDocument(document)
    
    suspend fun deleteDocumentById(id: Long) = documentDao.deleteDocumentById(id)
    
    // ========== Search ==========
    
    data class SearchResult(
        val id: Long,
        val title: String,
        val type: SearchResultType,
        val spotId: Long,
        val spotCode: String,
        val breadcrumb: String,
        val updatedAt: Long
    )
    
    enum class SearchResultType {
        ITEM, DOCUMENT
    }
    
    suspend fun globalSearch(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        try {
            val items = searchItems(query)
            val documents = searchDocuments(query)
            
            val results = mutableListOf<SearchResult>()
            
            for (item in items) {
                try {
                    val breadcrumb = getBreadcrumbForSpot(item.spotId)
                    val spot = getSpotById(item.spotId)
                    results.add(
                        SearchResult(
                            id = item.id,
                            title = item.name,
                            type = SearchResultType.ITEM,
                            spotId = item.spotId,
                            spotCode = spot?.code ?: "",
                            breadcrumb = breadcrumb,
                            updatedAt = item.updatedAt
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            for (doc in documents) {
                try {
                    val breadcrumb = getBreadcrumbForSpot(doc.spotId)
                    val spot = getSpotById(doc.spotId)
                    results.add(
                        SearchResult(
                            id = doc.id,
                            title = doc.title,
                            type = SearchResultType.DOCUMENT,
                            spotId = doc.spotId,
                            spotCode = spot?.code ?: "",
                            breadcrumb = breadcrumb,
                            updatedAt = doc.updatedAt
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            results.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getBreadcrumbForSpot(spotId: Long): String {
        val spot = getSpotById(spotId) ?: return ""
        val container = getContainerById(spot.containerId) ?: return spot.label
        val room = getRoomById(container.roomId) ?: return "${container.name} > ${spot.label}"
        return "${room.name} > ${container.name} > ${spot.label}"
    }
    
    // ========== Favorites ==========
    
    data class FavoriteItem(
        val id: Long,
        val name: String,
        val type: FavoriteType,
        val breadcrumb: String
    )
    
    enum class FavoriteType {
        CONTAINER, SPOT
    }
    
    fun getFavorites(): Flow<List<FavoriteItem>> {
        return combine(
            getFavoriteContainers(),
            getFavoriteSpots()
        ) { containers, spots ->
            val favorites = mutableListOf<FavoriteItem>()
            
            // This is a simplified version - in real code you'd need to fetch room names
            for (container in containers) {
                favorites.add(
                    FavoriteItem(
                        id = container.id,
                        name = container.name,
                        type = FavoriteType.CONTAINER,
                        breadcrumb = container.name
                    )
                )
            }
            
            for (spot in spots) {
                favorites.add(
                    FavoriteItem(
                        id = spot.id,
                        name = spot.label,
                        type = FavoriteType.SPOT,
                        breadcrumb = spot.label
                    )
                )
            }
            
            favorites
        }
    }
    
    // ========== Recent Items and Documents ==========
    
    data class RecentEntry(
        val id: Long,
        val title: String,
        val type: SearchResultType,
        val breadcrumb: String,
        val updatedAt: Long
    )
    
    suspend fun getRecentEntries(limit: Int = 10): List<RecentEntry> {
        return try {
            val items = itemDao.getAllItemsList().sortedByDescending { it.updatedAt }.take(limit)
            val documents = documentDao.getAllDocumentsList().sortedByDescending { it.updatedAt }.take(limit)
            
            val entries = mutableListOf<RecentEntry>()
            
            for (item in items) {
                try {
                    val breadcrumb = getBreadcrumbForSpot(item.spotId)
                    entries.add(
                        RecentEntry(
                            id = item.id,
                            title = item.name,
                            type = SearchResultType.ITEM,
                            breadcrumb = breadcrumb,
                            updatedAt = item.updatedAt
                        )
                    )
                } catch (e: Exception) {
                    // Skip this item if there's an error
                }
            }
            
            for (doc in documents) {
                try {
                    val breadcrumb = getBreadcrumbForSpot(doc.spotId)
                    entries.add(
                        RecentEntry(
                            id = doc.id,
                            title = doc.title,
                            type = SearchResultType.DOCUMENT,
                            breadcrumb = breadcrumb,
                            updatedAt = doc.updatedAt
                        )
                    )
                } catch (e: Exception) {
                    // Skip this document if there's an error
                }
            }
            
            entries.sortedByDescending { it.updatedAt }.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ========== Backup / Restore ==========
    
    suspend fun getAllDataForBackup(): BackupData {
        return BackupData(
            rooms = houseRoomDao.getAllRoomsList(),
            containers = containerDao.getAllContainersList(),
            spots = spotDao.getAllSpotsList(),
            items = itemDao.getAllItemsList(),
            documents = documentDao.getAllDocumentsList()
        )
    }
    
    suspend fun restoreFromBackup(data: BackupData) {
        // Clear all existing data
        documentDao.deleteAll()
        itemDao.deleteAll()
        spotDao.deleteAll()
        containerDao.deleteAll()
        houseRoomDao.deleteAll()
        
        // Insert backup data
        for (room in data.rooms) {
            houseRoomDao.insertRoom(room)
        }
        for (container in data.containers) {
            containerDao.insertContainer(container)
        }
        for (spot in data.spots) {
            spotDao.insertSpot(spot)
        }
        for (item in data.items) {
            itemDao.insertItem(item)
        }
        for (document in data.documents) {
            documentDao.insertDocument(document)
        }
    }
    
    suspend fun clearAllData() {
        documentDao.deleteAll()
        itemDao.deleteAll()
        spotDao.deleteAll()
        containerDao.deleteAll()
        houseRoomDao.deleteAll()
    }
    
    data class BackupData(
        val rooms: List<HouseRoomEntity>,
        val containers: List<ContainerEntity>,
        val spots: List<SpotEntity>,
        val items: List<ItemEntity>,
        val documents: List<DocumentEntity>
    )
}
