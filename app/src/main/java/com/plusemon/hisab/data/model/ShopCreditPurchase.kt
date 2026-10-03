package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shop_credit_purchases",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vendor::class,
            parentColumns = ["id"],
            childColumns = ["vendorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("vendorId")]
)
data class ShopCreditPurchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val vendorId: Long,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)
}
