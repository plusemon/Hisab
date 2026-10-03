package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Frequency(val labelEn: String, val labelBn: String) {
    DAILY("Daily", "দৈনিক"),
    WEEKLY("Weekly", "সাপ্তাহিক"),
    MONTHLY("Monthly", "মাসিক")
}

@Entity(
    tableName = "recurring_rules",
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
data class RecurringRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val accountId: Long,
    val categoryId: Long? = null,
    val amount: Double,
    val type: TransactionType,
    val frequency: Frequency,
    val startDate: Long,
    val endDate: Long? = null,
    val lastGeneratedDate: Long = 0L,
    val isPaused: Boolean = false,
    val note: String = ""
)
