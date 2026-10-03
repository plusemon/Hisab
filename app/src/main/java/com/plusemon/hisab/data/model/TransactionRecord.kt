package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("userId"),
        Index("accountId"),
        Index("categoryId"),
        Index("dateTimestamp")
    ]
)
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val accountId: Long,
    val categoryId: Long? = null, // null for transfers
    val toAccountId: Long? = null, // only used for transfers
    val amount: Double,
    val fee: Double = 0.0, // optional transfer or service fee
    val type: TransactionType,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val receiptUri: String? = null,
    val exchangeRate: Double = 1.0, // for multi-currency transfers
    val isRecurringInstance: Boolean = false,
    val recurringRuleId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    val transaction: TransactionRecord,
    val account: UserAccount,
    val toAccount: UserAccount? = null,
    val category: Category? = null
)
