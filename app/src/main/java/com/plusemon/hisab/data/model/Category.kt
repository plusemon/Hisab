package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType(val labelEn: String, val labelBn: String) {
    EXPENSE("Expense", "খরচ"),
    INCOME("Income", "আয়"),
    TRANSFER("Transfer", "স্থানান্তর")
}

@Entity(
    tableName = "categories",
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
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val nameBn: String,
    val nameEn: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
) {
    fun localizedName(isBangla: Boolean): String = if (isBangla) nameBn else nameEn
}
