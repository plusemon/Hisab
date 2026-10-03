package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountType(val labelEn: String, val labelBn: String) {
    CASH("Cash", "নগদ"),
    BANK("Bank", "ব্যাংক"),
    MOBILE_WALLET("Mobile Wallet", "মোবাইল ওয়ালেট"),
    CARD("Card", "কার্ড"),
    OTHER("Other", "অন্যান্য")
}

@Entity(
    tableName = "accounts",
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
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val type: AccountType,
    val currencyCode: String = "BDT",
    val startingBalance: Double = 0.0,
    val colorHex: String = "#0F766E",
    val iconName: String = "account_balance_wallet",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
