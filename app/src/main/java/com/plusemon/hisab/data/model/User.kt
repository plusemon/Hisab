package com.plusemon.hisab.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isGoogleUser: Boolean = false,
    val passwordHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
