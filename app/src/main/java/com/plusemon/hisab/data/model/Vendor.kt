package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vendors",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val phone: String = "",
    val locationNote: String = "",
    val categoryTag: String = "", // e.g. Grocery, Pharmacy, Hardware
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
