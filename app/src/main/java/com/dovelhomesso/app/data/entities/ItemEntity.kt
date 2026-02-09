package com.dovelhomesso.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
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
        Index(value = ["name"]),
        Index(value = ["tags"]),
        Index(value = ["keywords"]),
        Index(value = ["category"]),
        Index(value = ["updatedAt"])
    ]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val spotId: Long,
    val category: String? = null,
    val keywords: String? = null,
    val tags: String? = null,
    val note: String? = null,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
