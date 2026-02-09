package com.dovelhomesso.app.data.exportimport

import android.content.Context
import android.net.Uri
import com.dovelhomesso.app.data.entities.*
import com.dovelhomesso.app.data.repositories.AppRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val context: Context,
    private val repository: AppRepository
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    companion object {
        const val SCHEMA_VERSION = 3
        const val FILE_EXTENSION = "json"
        const val MIME_TYPE = "application/json"
    }
    
    data class BackupFile(
        val schemaVersion: Int = SCHEMA_VERSION,
        val appName: String = "DoveLhoMesso",
        val exportedAt: Long = System.currentTimeMillis(),
        val data: BackupDataDto
    )
    
    data class BackupDataDto(
        val rooms: List<HouseRoomDto>,
        val containers: List<ContainerDto>,
        val spots: List<SpotDto>,
        val items: List<ItemDto>,
        val documents: List<DocumentDto>
    )
    
    // DTOs for JSON serialization (same structure as entities but serializable)
    data class HouseRoomDto(
        val id: Long,
        val name: String,
        val icon: String?,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    data class ContainerDto(
        val id: Long,
        val roomId: Long,
        val name: String,
        val type: String,
        val note: String?,
        val isFavorite: Boolean,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    data class SpotDto(
        val id: Long,
        val containerId: Long,
        val label: String,
        val code: String,
        val note: String?,
        val isFavorite: Boolean,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    data class ItemDto(
        val id: Long,
        val name: String,
        val spotId: Long,
        val category: String?,
        val keywords: String?,
        val tags: String?,
        val note: String?,
        val imagePath: String?,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    data class DocumentDto(
        val id: Long,
        val title: String,
        val spotId: Long,
        val docType: String?,
        val person: String?,
        val expiryDate: Long?,
        val tags: String?,
        val note: String?,
        val filePath: String? = null,
        val filePaths: String? = null,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    /**
     * Exports all data to a JSON file at the specified URI.
     */
    suspend fun exportToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = repository.getAllDataForBackup()
            
            val backupFile = BackupFile(
                data = BackupDataDto(
                    rooms = backupData.rooms.map { it.toDto() },
                    containers = backupData.containers.map { it.toDto() },
                    spots = backupData.spots.map { it.toDto() },
                    items = backupData.items.map { it.toDto() },
                    documents = backupData.documents.map { it.toDto() }
                )
            )
            
            val json = gson.toJson(backupFile)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open output stream"))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Imports data from a JSON file at the specified URI.
     * Replaces all existing data.
     */
    suspend fun importFromUri(uri: Uri): Result<ImportStats> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Cannot open input stream"))
            
            val backupFile = gson.fromJson(json, BackupFile::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file format"))
            
            // Validate schema version
            if (backupFile.schemaVersion > SCHEMA_VERSION) {
                return@withContext Result.failure(Exception("Backup file is from a newer version of the app"))
            }
            
            // Convert DTOs to entities
            val rooms = backupFile.data.rooms.map { it.toEntity() }
            val containers = backupFile.data.containers.map { it.toEntity() }
            val spots = backupFile.data.spots.map { it.toEntity() }
            val items = backupFile.data.items.map { it.toEntity() }
            val documents = backupFile.data.documents.map { it.toEntity() }
            
            // Restore data
            repository.restoreFromBackup(
                AppRepository.BackupData(
                    rooms = rooms,
                    containers = containers,
                    spots = spots,
                    items = items,
                    documents = documents
                )
            )
            
            Result.success(
                ImportStats(
                    roomsCount = rooms.size,
                    containersCount = containers.size,
                    spotsCount = spots.size,
                    itemsCount = items.size,
                    documentsCount = documents.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generates a suggested filename for backup.
     */
    fun generateBackupFilename(): String {
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        return "dovelhomesso_backup_$timestamp.$FILE_EXTENSION"
    }
    
    data class ImportStats(
        val roomsCount: Int,
        val containersCount: Int,
        val spotsCount: Int,
        val itemsCount: Int,
        val documentsCount: Int
    ) {
        val totalCount: Int get() = roomsCount + containersCount + spotsCount + itemsCount + documentsCount
    }
    
    // Extension functions for DTO conversion
    
    private fun HouseRoomEntity.toDto() = HouseRoomDto(
        id = id, name = name, icon = icon, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun HouseRoomDto.toEntity() = HouseRoomEntity(
        id = id, name = name, icon = icon, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun ContainerEntity.toDto() = ContainerDto(
        id = id, roomId = roomId, name = name, type = type, note = note,
        isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun ContainerDto.toEntity() = ContainerEntity(
        id = id, roomId = roomId, name = name, type = type, note = note,
        isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun SpotEntity.toDto() = SpotDto(
        id = id, containerId = containerId, label = label, code = code, note = note,
        isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun SpotDto.toEntity() = SpotEntity(
        id = id, containerId = containerId, label = label, code = code, note = note,
        isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun ItemEntity.toDto() = ItemDto(
        id = id, name = name, spotId = spotId, category = category,
        keywords = keywords, tags = tags, note = note, imagePath = imagePath,
        createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun ItemDto.toEntity() = ItemEntity(
        id = id, name = name, spotId = spotId, category = category,
        keywords = keywords, tags = tags, note = note, imagePath = imagePath,
        createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun DocumentEntity.toDto() = DocumentDto(
        id = id, title = title, spotId = spotId, docType = docType,
        person = person, expiryDate = expiryDate, tags = tags, note = note,
        filePaths = filePaths,
        createdAt = createdAt, updatedAt = updatedAt
    )
    
    private fun DocumentDto.toEntity(): DocumentEntity {
        val finalFilePaths = if (!filePaths.isNullOrBlank()) {
            filePaths
        } else if (!filePath.isNullOrBlank()) {
            // Convert legacy single path to JSON list
            try {
                gson.toJson(listOf(filePath))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        return DocumentEntity(
            id = id, title = title, spotId = spotId, docType = docType,
            person = person, expiryDate = expiryDate, tags = tags, note = note,
            filePaths = finalFilePaths,
            createdAt = createdAt, updatedAt = updatedAt
        )
    }
}
