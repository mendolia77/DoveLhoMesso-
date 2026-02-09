package com.dovelhomesso.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dovelhomesso.app.data.dao.*
import com.dovelhomesso.app.data.entities.*

@Database(
    entities = [
        HouseRoomEntity::class,
        ContainerEntity::class,
        SpotEntity::class,
        ItemEntity::class,
        DocumentEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun houseRoomDao(): HouseRoomDao
    abstract fun containerDao(): ContainerDao
    abstract fun spotDao(): SpotDao
    abstract fun itemDao(): ItemDao
    abstract fun documentDao(): DocumentDao
    
    companion object {
        private const val DATABASE_NAME = "dovelhomesso.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
