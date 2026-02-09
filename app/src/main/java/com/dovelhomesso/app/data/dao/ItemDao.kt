package com.dovelhomesso.app.data.dao

import androidx.room.*
import com.dovelhomesso.app.data.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE spotId = :spotId ORDER BY name ASC")
    fun getItemsBySpot(spotId: Long): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE spotId = :spotId ORDER BY name ASC")
    suspend fun getItemsBySpotList(spotId: Long): List<ItemEntity>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?
    
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemByIdFlow(id: Long): Flow<ItemEntity?>
    
    @Query("SELECT * FROM items ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentItems(limit: Int = 10): Flow<List<ItemEntity>>
    
    @Query("""
        SELECT * FROM items 
        WHERE name LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%' 
           OR keywords LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    suspend fun searchItems(query: String): List<ItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long
    
    @Update
    suspend fun updateItem(item: ItemEntity)
    
    @Delete
    suspend fun deleteItem(item: ItemEntity)
    
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
    
    @Query("SELECT COUNT(*) FROM items WHERE spotId = :spotId")
    suspend fun getItemCountBySpot(spotId: Long): Int
    
    @Query("SELECT * FROM items")
    suspend fun getAllItemsList(): List<ItemEntity>
    
    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
