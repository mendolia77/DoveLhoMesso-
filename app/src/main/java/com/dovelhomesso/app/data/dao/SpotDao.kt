package com.dovelhomesso.app.data.dao

import androidx.room.*
import com.dovelhomesso.app.data.entities.SpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {
    
    @Query("SELECT * FROM spots ORDER BY label ASC")
    fun getAllSpots(): Flow<List<SpotEntity>>
    
    @Query("SELECT * FROM spots WHERE containerId = :containerId ORDER BY label ASC")
    fun getSpotsByContainer(containerId: Long): Flow<List<SpotEntity>>
    
    @Query("SELECT * FROM spots WHERE containerId = :containerId ORDER BY label ASC")
    suspend fun getSpotsByContainerList(containerId: Long): List<SpotEntity>
    
    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Long): SpotEntity?
    
    @Query("SELECT * FROM spots WHERE id = :id")
    fun getSpotByIdFlow(id: Long): Flow<SpotEntity?>
    
    @Query("SELECT * FROM spots WHERE code = :code")
    suspend fun getSpotByCode(code: String): SpotEntity?
    
    @Query("SELECT * FROM spots WHERE isFavorite = 1 ORDER BY label ASC")
    fun getFavoriteSpots(): Flow<List<SpotEntity>>
    
    @Query("SELECT * FROM spots WHERE label LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    suspend fun searchSpots(query: String): List<SpotEntity>
    
    @Query("SELECT code FROM spots WHERE code LIKE :prefix || '%'")
    suspend fun getCodesWithPrefix(prefix: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: SpotEntity): Long
    
    @Update
    suspend fun updateSpot(spot: SpotEntity)
    
    @Delete
    suspend fun deleteSpot(spot: SpotEntity)
    
    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteSpotById(id: Long)
    
    @Query("UPDATE spots SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM spots")
    suspend fun getAllSpotsList(): List<SpotEntity>
    
    @Query("DELETE FROM spots")
    suspend fun deleteAll()
}
