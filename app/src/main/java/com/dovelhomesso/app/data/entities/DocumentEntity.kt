package com.dovelhomesso.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = SpotEntity::class,
            parentColumns = ["id"],
            childColumns = ["spotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["spotId"]),
        Index(value = ["title"]),
        Index(value = ["tags"]),
        Index(value = ["docType"]),
        Index(value = ["person"]),
        Index(value = ["expiryDate"]),
        Index(value = ["updatedAt"])
    ]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val spotId: Long,
    val docType: String? = null,
    val person: String? = null,
    val expiryDate: Long? = null,
    val tags: String? = null,
    val note: String? = null,
    val filePaths: String? = null, // JSON list of file paths
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
