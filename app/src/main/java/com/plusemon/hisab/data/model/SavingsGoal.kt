package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
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
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentSavedAmount: Double = 0.0,
    val targetDate: Long? = null,
    val note: String = "",
    val colorHex: String = "#0D9488",
    val iconName: String = "savings",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
