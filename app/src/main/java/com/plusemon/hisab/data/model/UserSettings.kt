package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val userId: String,
    val language: String = "bn", // "bn" or "en"
    val numeralSystem: String = "bn", // "bn" for ১২৩ or "en" for 123
    val defaultCurrency: String = "BDT", // "BDT", "USD", "EUR", etc.
    val currencySymbol: String = "৳",
    val hideBalances: Boolean = false,
    val pinEnabled: Boolean = false,
    val pinCode: String = "",
    val dailyReminderEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val useSystemTheme: Boolean = true,
    val syncStatus: String = "SYNCED" // "SYNCED", "SYNCING", "OFFLINE"
)
