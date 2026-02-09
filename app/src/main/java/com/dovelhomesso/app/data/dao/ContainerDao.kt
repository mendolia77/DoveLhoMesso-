package com.dovelhomesso.app.data.dao

import androidx.room.*
import com.dovelhomesso.app.data.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    
    @Query("SELECT * FROM containers ORDER BY name ASC")
    fun getAllContainers(): Flow<List<ContainerEntity>>
    
    @Query("SELECT * FROM containers WHERE roomId = :roomId ORDER BY name ASC")
    fun getContainersByRoom(roomId: Long): Flow<List<ContainerEntity>>
    
    @Query("SELECT * FROM containers WHERE roomId = :roomId ORDER BY name ASC")
    suspend fun getContainersByRoomList(roomId: Long): List<ContainerEntity>
    
    @Query("SELECT * FROM containers WHERE id = :id")
    suspend fun getContainerById(id: Long): ContainerEntity?
    
    @Query("SELECT * FROM containers WHERE id = :id")
    fun getContainerByIdFlow(id: Long): Flow<ContainerEntity?>
    
    @Query("SELECT * FROM containers WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContainers(): Flow<List<ContainerEntity>>
    
    @Query("SELECT * FROM containers WHERE name LIKE '%' || :query || '%'")
    suspend fun searchContainers(query: String): List<ContainerEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: ContainerEntity): Long
    
    @Update
    suspend fun updateContainer(container: ContainerEntity)
    
    @Delete
    suspend fun deleteContainer(container: ContainerEntity)
    
    @Query("DELETE FROM containers WHERE id = :id")
    suspend fun deleteContainerById(id: Long)
    
    @Query("UPDATE containers SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM containers")
    suspend fun getAllContainersList(): List<ContainerEntity>
    
    @Query("DELETE FROM containers")
    suspend fun deleteAll()
}
