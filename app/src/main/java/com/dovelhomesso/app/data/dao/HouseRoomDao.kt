package com.dovelhomesso.app.data.dao

import androidx.room.*
import com.dovelhomesso.app.data.entities.HouseRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseRoomDao {
    
    @Query("SELECT * FROM house_rooms ORDER BY name ASC")
    fun getAllRooms(): Flow<List<HouseRoomEntity>>
    
    @Query("SELECT * FROM house_rooms ORDER BY updatedAt DESC")
    fun getAllRoomsByRecent(): Flow<List<HouseRoomEntity>>
    
    @Query("SELECT * FROM house_rooms WHERE id = :id")
    suspend fun getRoomById(id: Long): HouseRoomEntity?
    
    @Query("SELECT * FROM house_rooms WHERE id = :id")
    fun getRoomByIdFlow(id: Long): Flow<HouseRoomEntity?>
    
    @Query("SELECT * FROM house_rooms WHERE name LIKE '%' || :query || '%'")
    suspend fun searchRooms(query: String): List<HouseRoomEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: HouseRoomEntity): Long
    
    @Update
    suspend fun updateRoom(room: HouseRoomEntity)
    
    @Delete
    suspend fun deleteRoom(room: HouseRoomEntity)
    
    @Query("DELETE FROM house_rooms WHERE id = :id")
    suspend fun deleteRoomById(id: Long)
    
    @Query("SELECT COUNT(*) FROM house_rooms")
    suspend fun getRoomCount(): Int
    
    @Query("SELECT * FROM house_rooms")
    suspend fun getAllRoomsList(): List<HouseRoomEntity>
    
    @Query("DELETE FROM house_rooms")
    suspend fun deleteAll()
}
