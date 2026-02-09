package com.dovelhomesso.app.data.dao

import androidx.room.*
import com.dovelhomesso.app.data.entities.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    
    @Query("SELECT * FROM documents ORDER BY title ASC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>
    
    @Query("SELECT * FROM documents WHERE spotId = :spotId ORDER BY title ASC")
    fun getDocumentsBySpot(spotId: Long): Flow<List<DocumentEntity>>
    
    @Query("SELECT * FROM documents WHERE spotId = :spotId ORDER BY title ASC")
    suspend fun getDocumentsBySpotList(spotId: Long): List<DocumentEntity>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?
    
    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentByIdFlow(id: Long): Flow<DocumentEntity?>
    
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>>
    
    @Query("SELECT * FROM documents WHERE expiryDate IS NOT NULL AND expiryDate > 0 ORDER BY expiryDate ASC")
    fun getDocumentsWithExpiry(): Flow<List<DocumentEntity>>
    
    @Query("""
        SELECT * FROM documents 
        WHERE title LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%' 
           OR docType LIKE '%' || :query || '%'
           OR person LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    suspend fun searchDocuments(query: String): List<DocumentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long
    
    @Update
    suspend fun updateDocument(document: DocumentEntity)
    
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
    
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
    
    @Query("SELECT COUNT(*) FROM documents WHERE spotId = :spotId")
    suspend fun getDocumentCountBySpot(spotId: Long): Int
    
    @Query("SELECT * FROM documents")
    suspend fun getAllDocumentsList(): List<DocumentEntity>
    
    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}
