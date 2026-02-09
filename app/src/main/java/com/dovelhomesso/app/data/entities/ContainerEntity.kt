package com.dovelhomesso.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "containers",
    foreignKeys = [
        ForeignKey(
            entity = HouseRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["roomId"]),
        Index(value = ["name"]),
        Index(value = ["isFavorite"]),
        Index(value = ["updatedAt"])
    ]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val name: String,
    val type: String = ContainerType.ALTRO.name,
    val note: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ContainerType {
    ARMADIO,
    CASSETTO,
    SCAFFALE,
    CARTELLINA,
    ALTRO
}
