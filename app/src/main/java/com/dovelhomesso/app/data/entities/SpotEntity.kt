package com.dovelhomesso.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spots",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["containerId"]),
        Index(value = ["label"]),
        Index(value = ["code"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["updatedAt"])
    ]
)
data class SpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val containerId: Long,
    val label: String,
    val code: String,
    val note: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
