package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shop_credit_payments",
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
        ),
        ForeignKey(
            entity = UserAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("vendorId"), Index("accountId")]
)
data class ShopCreditPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val vendorId: Long,
    val accountId: Long,
    val amount: Double,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val transactionId: Long? = null
)
