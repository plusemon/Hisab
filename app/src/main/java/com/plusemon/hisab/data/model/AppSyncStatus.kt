package com.plusemon.hisab.data.model

sealed class AppSyncStatus {
    object Idle : AppSyncStatus()
    object Saving : AppSyncStatus()
    data class Saved(val timestamp: Long = System.currentTimeMillis()) : AppSyncStatus()
    object Syncing : AppSyncStatus()
    data class Synced(val timestamp: Long = System.currentTimeMillis()) : AppSyncStatus()
    object CheckingUpdates : AppSyncStatus()
    data class Error(val message: String? = null) : AppSyncStatus()
}
